package com.socialapp.repository;

import com.socialapp.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /** Feed: bài của những người đang follow + của chính mình */
    @Query("""
            SELECT p FROM Post p
            WHERE p.user.id IN (
                SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId
            )
            OR p.user.id = :userId
            ORDER BY p.createdAt DESC
            """)
    Page<Post> findFeedByUserId(@Param("userId") Long userId, Pageable pageable);

    /** Lấy tất cả bài của một user, mới nhất trước */
    Page<Post> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Tìm bài viết theo nội dung (không phân biệt hoa thường).
     * Không lấy bài repost. Giới hạn 20 kết quả.
     */
    @Query("""
            SELECT p FROM Post p
            WHERE p.isRepost = false
              AND LOWER(p.content) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY p.createdAt DESC
            """)
    List<Post> searchByContent(@Param("q") String q, Pageable pageable);
}