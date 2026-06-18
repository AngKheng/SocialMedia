package com.socialapp.repository;

import com.socialapp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Lấy toàn bộ lịch sử chat giữa 2 người,
     * sắp xếp theo thời gian tăng dần.
     */
    @Query("""
            SELECT m FROM Message m
            WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2)
               OR (m.sender.id = :userId2 AND m.receiver.id = :userId1)
            ORDER BY m.createdAt ASC
            """)
    List<Message> findConversation(@Param("userId1") Long userId1,
                                   @Param("userId2") Long userId2);

    /** Đếm tin nhắn chưa đọc mà userId nhận được */
    long countByReceiverIdAndIsRead(Long receiverId, Boolean isRead);

    /**
     * Đánh dấu đã đọc toàn bộ tin nhắn gửi TỪ otherUserId ĐẾN userId.
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE Message m SET m.isRead = true
            WHERE m.receiver.id = :userId
              AND m.sender.id = :otherUserId
              AND m.isRead = false
            """)
    void markConversationAsRead(@Param("userId") Long userId,
                                @Param("otherUserId") Long otherUserId);

    /**
     * Lấy toàn bộ message mà userId tham gia (gửi hoặc nhận),
     * mới nhất trước — dùng để build danh sách hội thoại (conversation list).
     * Xử lý group-by ở service layer vì JPQL group-by phức tạp với entity.
     */
    @Query("""
            SELECT m FROM Message m
            WHERE m.sender.id = :userId OR m.receiver.id = :userId
            ORDER BY m.createdAt DESC
            """)
    List<Message> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}