package com.socialapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    // ── Enum khai báo ngay trong Entity để không cần file riêng ──
    public enum Type {
        LIKE,        // Ai đó like bài của bạn
        COMMENT,     // Ai đó comment bài của bạn
        FOLLOW,      // Ai đó follow bạn
        MENTION,     // Ai đó @mention bạn trong comment
        GROQ_REPLY,  // Groq AI đã trả lời @groq của bạn
        NEW_MESSAGE  // Bạn có tin nhắn mới
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Người nhận thông báo */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_notifications_user"))
    private User user;

    /**
     * Người thực hiện hành động.
     * Có thể null nếu là thông báo hệ thống.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id",
                foreignKey = @ForeignKey(name = "fk_notifications_actor"))
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Type type;

    /**
     * ID của object liên quan:
     *   LIKE / COMMENT / MENTION / GROQ_REPLY → postId
     *   FOLLOW                                → followerId
     *   NEW_MESSAGE                           → messageId
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /** Nội dung thông báo hiển thị cho người dùng */
    @Column(length = 500)
    private String message;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}