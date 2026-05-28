package com.socialapp.repository;

import com.socialapp.model.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {

    /**
     * Lấy toàn bộ lịch sử hội thoại của một user trong một post,
     * sắp xếp theo thời gian để build context cho Groq API.
     */
    List<AiConversation> findByPostIdAndUserIdOrderByCreatedAtAsc(Long postId, Long userId);

    /** Đếm số lượt hội thoại — dùng để giới hạn context nếu quá dài */
    long countByPostIdAndUserId(Long postId, Long userId);
}