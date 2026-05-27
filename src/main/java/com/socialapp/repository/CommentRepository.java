package com.socialapp.repository;

import com.socialapp.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Lấy comment gốc (không phải reply) của một post
    List<Comment> findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(Long postId);

    // Đếm số comment của một post
    long countByPostId(Long postId);
}