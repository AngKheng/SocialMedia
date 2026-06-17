package com.socialapp.service;

import com.socialapp.dto.response.LikeResponse;
import com.socialapp.exception.ResourceNotFoundException;
import com.socialapp.model.Comment;
import com.socialapp.model.Like;
import com.socialapp.model.Post;
import com.socialapp.model.User;
import com.socialapp.repository.CommentRepository;
import com.socialapp.repository.LikeRepository;
import com.socialapp.repository.PostRepository;
import com.socialapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {

    private final LikeRepository         likeRepository;
    private final PostRepository         postRepository;
    private final CommentRepository      commentRepository;
    private final UserRepository         userRepository;
    private final NotificationService    notificationService;   // ← thêm mới

    // =============================================
    // POST /api/posts/{id}/like
    // =============================================

    @Transactional
    public LikeResponse likePost(Long postId, UserDetails currentUser) {
        User me   = getUser(currentUser.getUsername());
        Post post = getPost(postId);

        if (likeRepository.existsByUserIdAndPostId(me.getId(), postId)) {
            throw new IllegalArgumentException("Bạn đã like bài này rồi");
        }

        likeRepository.save(Like.builder().user(me).post(post).build());

        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);

        // Gửi thông báo (async)
        notificationService.notifyLikePost(me, post);

        log.info("@{} liked post id={}", me.getUsername(), postId);
        return new LikeResponse(true, post.getLikeCount());
    }

    // =============================================
    // DELETE /api/posts/{id}/like
    // =============================================

    @Transactional
    public LikeResponse unlikePost(Long postId, UserDetails currentUser) {
        User me   = getUser(currentUser.getUsername());
        Post post = getPost(postId);

        if (!likeRepository.existsByUserIdAndPostId(me.getId(), postId)) {
            throw new IllegalArgumentException("Bạn chưa like bài này");
        }

        likeRepository.deleteByUserIdAndPostId(me.getId(), postId);

        post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        postRepository.save(post);

        log.info("@{} unliked post id={}", me.getUsername(), postId);
        return new LikeResponse(false, post.getLikeCount());
    }

    // =============================================
    // POST /api/comments/{id}/like
    // =============================================

    @Transactional
    public LikeResponse likeComment(Long commentId, UserDetails currentUser) {
        User    me      = getUser(currentUser.getUsername());
        Comment comment = getComment(commentId);

        if (likeRepository.existsByUserIdAndCommentId(me.getId(), commentId)) {
            throw new IllegalArgumentException("Bạn đã like comment này rồi");
        }

        likeRepository.save(Like.builder().user(me).comment(comment).build());

        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);

        log.info("@{} liked comment id={}", me.getUsername(), commentId);
        return new LikeResponse(true, comment.getLikeCount());
    }

    // =============================================
    // DELETE /api/comments/{id}/like
    // =============================================

    @Transactional
    public LikeResponse unlikeComment(Long commentId, UserDetails currentUser) {
        User    me      = getUser(currentUser.getUsername());
        Comment comment = getComment(commentId);

        if (!likeRepository.existsByUserIdAndCommentId(me.getId(), commentId)) {
            throw new IllegalArgumentException("Bạn chưa like comment này");
        }

        likeRepository.deleteByUserIdAndCommentId(me.getId(), commentId);

        comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
        commentRepository.save(comment);

        log.info("@{} unliked comment id={}", me.getUsername(), commentId);
        return new LikeResponse(false, comment.getLikeCount());
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

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));
    }
}