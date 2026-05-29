package com.socialapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_notif_user_read", columnList = "user_id, is_read"),
        @Index(name = "idx_notif_user_created", columnList = "user_id, created_at DESC")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    public enum Type {
        LIKE,        // Like bài / comment
        COMMENT,     // Comment vào bài
        FOLLOW,      // Follow mình
        MENTION,     // @mention trong comment
        GROQ_REPLY,  // Groq AI đã trả lời
        NEW_MESSAGE  // Tin nhắn mới
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Người nhận thông báo */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_notifications_user"))
    private User user;

    /** Người thực hiện hành động */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id",
                foreignKey = @ForeignKey(name = "fk_notifications_actor"))
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Type type;

    /** Post liên quan (nếu có) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id",
                foreignKey = @ForeignKey(name = "fk_notifications_post"))
    private Post post;

    /** Comment liên quan (nếu có) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id",
                foreignKey = @ForeignKey(name = "fk_notifications_comment"))
    private Comment comment;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}