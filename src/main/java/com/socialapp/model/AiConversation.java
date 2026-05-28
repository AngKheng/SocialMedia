package com.socialapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "ai_conversations",
    indexes = {
        // Index để query context nhanh theo post + user
        @Index(name = "idx_ai_conv_post_user", columnList = "post_id, user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiConversation {

    public enum Role {
        user,       // Tin nhắn người dùng gửi cho Groq
        assistant   // Phản hồi của Groq AI
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Context gắn với bài post nào.
     * Mỗi post có thể có nhiều luồng hội thoại với Groq
     * (mỗi người dùng một luồng riêng).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_ai_conv_post"))
    private Post post;

    /** Người dùng đang hội thoại với Groq */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_ai_conv_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    /**
     * Nội dung tin nhắn.
     * NVARCHAR(MAX) để hỗ trợ tiếng Việt và nội dung dài.
     */
    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}