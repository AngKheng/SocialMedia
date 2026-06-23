package com.socialapp.service;

import com.socialapp.dto.request.CreateCommentRequest;
import com.socialapp.dto.request.UpdateCommentRequest;
import com.socialapp.dto.response.CommentResponse;
import com.socialapp.exception.ResourceNotFoundException;
import com.socialapp.model.Comment;
import com.socialapp.model.Post;
import com.socialapp.model.User;
import com.socialapp.repository.CommentRepository;
import com.socialapp.repository.PostRepository;
import com.socialapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

 private final CommentRepository commentRepository;
 private final PostRepository postRepository;
 private final UserRepository userRepository;
 private final AiMentionService aiMentionService;
 private final NotificationService notificationService;

 // =============================================
 // POST /api/comments
 // =============================================

 @Transactional
 public CommentResponse createComment(CreateCommentRequest request,
 UserDetails currentUser) {
 User me = getUser(currentUser.getUsername());
 Post post = getPost(request.postId());

 Comment parentComment = null;
 if (request.parentCommentId() != null) {
 parentComment = commentRepository.findById(request.parentCommentId())
 .orElseThrow(() -> new ResourceNotFoundException(
 "Comment", request.parentCommentId()));

 if (parentComment.getParentComment() != null) {
 throw new IllegalArgumentException(
 "Không thể reply vào một reply, chỉ reply vào comment gốc");
 }
 }

 Comment comment = Comment.builder()
 .post(post)
 .user(me)
 .content(request.content())
 .parentComment(parentComment)
 .isAiGenerated(false)
 .build();

 Comment saved = commentRepository.save(comment);

 post.setCommentCount(post.getCommentCount() + 1);
 postRepository.save(post);

 log.info("@{} commented on post id={}", me.getUsername(), post.getId());

 // Thông báo cho chủ bài (async)
 notificationService.notifyComment(me, post, saved);

 // Kiểm tra @groq mention (async)
 aiMentionService.handleIfMentioned(saved);

 return CommentResponse.from(saved);
 }

 // =============================================
 // GET /api/comments/post/{id}
 // =============================================

 @Transactional(readOnly = true)
 public List<CommentResponse> getCommentsByPost(Long postId) {
 getPost(postId);

 List<Comment> roots = commentRepository
 .findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(postId);

 return roots.stream()
 .map(root -> {
 List<CommentResponse> replies = commentRepository
 .findByParentCommentIdOrderByCreatedAtAsc(root.getId())
 .stream()
 .map(CommentResponse::from)
 .toList();
 return CommentResponse.from(root, replies);
 })
 .toList();
 }

 // =============================================
 // PUT /api/comments/{id}
 // =============================================

 @Transactional
 public CommentResponse updateComment(Long commentId, UpdateCommentRequest request,
 UserDetails currentUser) {
 Comment comment = commentRepository.findById(commentId)
 .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

 if (!comment.getUser().getUsername().equals(currentUser.getUsername())) {
 throw new IllegalArgumentException("Bạn không có quyền sửa comment này");
 }

 comment.setContent(request.content());
 Comment saved = commentRepository.save(comment);
 log.info("@{} đã cập nhật comment id={}", currentUser.getUsername(), commentId);

 return CommentResponse.from(saved);
 }

 // =============================================
 // DELETE /api/comments/{id}
 // =============================================

 @Transactional
 public void deleteComment(Long commentId, UserDetails currentUser) {
 Comment comment = commentRepository.findById(commentId)
 .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

 if (!comment.getUser().getUsername().equals(currentUser.getUsername())) {
 throw new IllegalArgumentException("Bạn không có quyền xóa comment này");
 }

 Post post = comment.getPost();
 long replyCount = commentRepository
 .findByParentCommentIdOrderByCreatedAtAsc(commentId).size();

 commentRepository.delete(comment);

 int toSubtract = (int) (1 + replyCount);
 post.setCommentCount(Math.max(0, post.getCommentCount() - toSubtract));
 postRepository.save(post);

 log.info("@{} đã xóa comment id={}", currentUser.getUsername(), commentId);
 }

 // =============================================
 // Helper
 // =============================================

 private User getUser(String username) {
 return userRepository.findByUsername(username)
 .orElseThrow(() -> new ResourceNotFoundException(
 "User không tồn tại: " + username));
 }

 private Post getPost(Long postId) {
 return postRepository.findById(postId)
 .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
 }
}
