package com.socialapp.service;

import com.socialapp.model.AiMention;
import com.socialapp.model.Comment;
import com.socialapp.model.Post;
import com.socialapp.model.User;
import com.socialapp.repository.AiMentionRepository;
import com.socialapp.repository.CommentRepository;
import com.socialapp.repository.PostRepository;
import com.socialapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiMentionService {

 private final AiMentionRepository aiMentionRepository;
 private final CommentRepository commentRepository;
 private final PostRepository postRepository;
 private final UserRepository userRepository;
 private final GroqService groqService;

 @Value("${groq.bot.username}")
 private String botUsername;

 /**
 * Public async entry point — chỉ chạy trên thread riêng.
 *
 * Lý do KHÔNG đặt @Transactional ở đây:
 * Spring AOP chỉ apply được 1 advice duy nhất trên 1 method
 * (Async HOẶC Transactional). Khi đặt cả 2 cùng lúc, transaction
 * không hoạt động đúng trên thread mới → lazy load fail,
 * exception bị nuốt hoàn toàn. Bug "Groq chỉ chạy 1 lần"
 * gốc là ở chỗ này.
 *
 * Toàn bộ exception được nuốt + log stack trace đầy đủ
 * để debug nếu có lỗi lần sau.
 */
 @Async
 public void handleIfMentioned(Comment comment) {
 try {
 handleIfMentionedInternal(comment);
 } catch (Exception e) {
 // Truyền Throwable ở cuối → SLF4J tự in stack trace đầy đủ
 log.error("Async @groq xử lý thất bại cho comment id={}",
 comment.getId(), e);
 }
 }

 /**
 * Logic chính — chạy trong transaction riêng (qua proxy).
 * Throw exception ra ngoài để caller (@Async) log.
 */
 @Transactional
 public void handleIfMentionedInternal(Comment comment) {
 String content = comment.getContent();

 // Kiểm tra có @groq không (case-insensitive)
 if (!content.toLowerCase().contains("@groq")) {
 return;
 }

 log.info("Phát hiện @groq trong comment id={}", comment.getId());

 // Strip @groq ra khỏi câu hỏi
 String question = content.replaceAll("(?i)@groq", "").trim();
 if (question.isBlank()) {
 question = "Xin chào!";
 }

 Post post = comment.getPost();
 User user = comment.getUser();

 // Lưu AiMention (processed = false) — commit ngay ở đây
 AiMention mention = AiMention.builder()
 .post(post)
 .comment(comment)
 .user(user)
 .mentionedText(question)
 .processed(false)
 .build();
 mention = aiMentionRepository.save(mention);

 try {
 // Gọi Groq API
 String aiReply = groqService.ask(post.getId(), user.getId(), question);

 // Lấy bot user
 User groqBot = userRepository.findByUsernameAndIsBot(botUsername, true)
 .orElseThrow(() -> new RuntimeException("Không tìm thấy Groq bot"));

 // Lưu reply của AI thành comment mới
 Comment aiComment = Comment.builder()
 .post(post)
 .user(groqBot)
 .content(aiReply)
 .parentComment(comment)  // reply vào comment của user
 .isAiGenerated(true)
 .build();
 aiComment = commentRepository.save(aiComment);

 // Cập nhật commentCount trên post
 post.setCommentCount(post.getCommentCount() + 1);
 postRepository.save(post);

 // Đánh dấu đã xử lý xong
 mention.setAiResponseComment(aiComment);
 mention.setProcessed(true);
 mention.setProcessedAt(LocalDateTime.now());
 aiMentionRepository.save(mention);

 log.info("Groq đã reply comment id={} trong post id={}",
 comment.getId(), post.getId());

 } catch (Exception e) {
 // Truyền Throwable ở cuối → in stack trace đầy đủ
 log.error("Groq xử lý thất bại cho comment id={}",
 comment.getId(), e);
 // Không throw — mention giữ processed=false để có thể retry sau
 }
 }
}
