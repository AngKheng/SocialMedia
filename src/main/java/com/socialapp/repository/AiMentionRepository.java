package com.socialapp.repository;

import com.socialapp.model.AiMention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiMentionRepository extends JpaRepository<AiMention, Long> {

    /**
     * Lấy toàn bộ lịch sử @groq của một user trong một post,
     * chỉ lấy những lần đã xử lý xong → dùng để build context cho Groq API.
     */
    List<AiMention> findByPostIdAndUserIdAndProcessedTrueOrderByCreatedAtAsc(
            Long postId, Long userId);

    /** Lấy mention chưa xử lý (nếu cần retry) */
    List<AiMention> findByProcessedFalseOrderByCreatedAtAsc();

    long countByPostIdAndUserId(Long postId, Long userId);
}