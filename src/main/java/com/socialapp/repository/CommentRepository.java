package com.socialapp.repository;

import com.socialapp.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Lấy các comment gốc của một post (không phải reply).
     * parentComment = null → là comment gốc.
     */
    List<Comment> findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(Long postId);

    /**
     * Lấy các reply của một comment cha.
     */
    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);

    /** Đếm tổng số comment (gốc + reply) của một post */
    long countByPostId(Long postId);
}