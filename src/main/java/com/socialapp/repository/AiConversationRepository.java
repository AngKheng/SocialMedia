package com.socialapp.repository;

import com.socialapp.model.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {

    // Lấy toàn bộ lịch sử chat của một user trong một post (để build context cho Groq)
    List<AiConversation> findByPostIdAndUserIdOrderByCreatedAtAsc(Long postId, Long userId);

    // Xóa context cũ nếu quá dài (optional)
    long countByPostIdAndUserId(Long postId, Long userId);
}