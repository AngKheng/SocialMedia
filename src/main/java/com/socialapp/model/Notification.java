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

    public enum Type {
        LIKE,           // Ai đó like bài của bạn
        COMMENT,        // Ai đó comment bài của bạn
        FOLLOW,         // Ai đó follow bạn
        MENTION,        // Ai đó @mention bạn
        GROQ_REPLY,     // Groq AI đã trả lời @groq của bạn
        NEW_MESSAGE     // Tin nhắn mới
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người nhận thông báo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Người thực hiện hành động (actor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Type type;

    // ID của object liên quan (postId, commentId, messageId, ...)
    @Column(name = "reference_id")
    private Long referenceId;

    // Nội dung thông báo hiển thị
    @Column(length = 500)
    private String message;

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}