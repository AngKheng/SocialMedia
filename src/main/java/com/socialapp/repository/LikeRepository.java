package com.socialapp.repository;

import com.socialapp.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    // ── Like Post ──────────────────────────────────────────────────────

    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);

    void deleteByUserIdAndPostId(Long userId, Long postId);

    // ── Like Comment ───────────────────────────────────────────────────

    Optional<Like> findByUserIdAndCommentId(Long userId, Long commentId);

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    long countByCommentId(Long commentId);

    void deleteByUserIdAndCommentId(Long userId, Long commentId);
}