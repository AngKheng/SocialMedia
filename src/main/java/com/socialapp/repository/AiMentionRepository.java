package com.socialapp.repository;

import com.socialapp.model.AiMention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiMentionRepository extends JpaRepository<AiMention, Long> {

    // ❌ CŨ — bị LazyInitializationException ở lần gọi thứ 2
    List<AiMention> findByPostIdAndUserIdAndProcessedTrueOrderByCreatedAtAsc(
            Long postId, Long userId);

    // ✅ MỚI — JOIN FETCH load luôn aiResponseComment trong 1 query
    @Query("""
            SELECT m FROM AiMention m
            LEFT JOIN FETCH m.aiResponseComment
            WHERE m.post.id = :postId
              AND m.user.id = :userId
              AND m.processed = true
            ORDER BY m.createdAt ASC
            """)
    List<AiMention> findHistoryWithResponse(@Param("postId") Long postId,
                                            @Param("userId") Long userId);

    List<AiMention> findByProcessedFalseOrderByCreatedAtAsc();

    long countByPostIdAndUserId(Long postId, Long userId);
}