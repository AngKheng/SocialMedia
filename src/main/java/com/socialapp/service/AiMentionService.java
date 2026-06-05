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
    private final CommentRepository   commentRepository;
    private final PostRepository      postRepository;
    private final UserRepository      userRepository;
    private final GroqService         groqService;

    @Value("${groq.bot.username}")
    private String botUsername;

    /**
     * Gọi sau khi tạo comment thành công.
     * Chạy async để không block response trả về cho user.
     *
     * Nếu comment không chứa @groq → bỏ qua ngay.
     */
    @Async
    @Transactional
    public void handleIfMentioned(Comment comment) {
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

        // Lưu AiMention (processed = false)
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
                    .parentComment(comment)   // reply vào comment của user
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
            log.error("Groq xử lý thất bại cho comment id={}: {}",
                    comment.getId(), e.getMessage());
            // Không throw — mention giữ processed=false để có thể retry sau
        }
    }
}