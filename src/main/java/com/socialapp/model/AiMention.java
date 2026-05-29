package com.socialapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Lưu lịch sử các lần user @groq trong comment.
 * Mỗi record = một lần mention + response của AI.
 *
 * Để nhớ context: load tất cả AiMention theo post_id + user_id,
 * build history từ mentioned_text (role=user) + comment.content (role=assistant).
 */
@Entity
@Table(
    name = "ai_mentions",
    indexes = {
        @Index(name = "idx_ai_mentions_post", columnList = "post_id, processed"),
        @Index(name = "idx_ai_mentions_user", columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiMention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Post chứa comment có @groq */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id",
                foreignKey = @ForeignKey(name = "fk_ai_mentions_post"))
    private Post post;

    /** Comment của user có chứa @groq */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id",
                foreignKey = @ForeignKey(name = "fk_ai_mentions_comment"))
    private Comment comment;

    /** User đã tag @groq */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_ai_mentions_user"))
    private User user;

    /** Nội dung câu hỏi sau khi strip @groq */
    @Column(name = "mentioned_text", columnDefinition = "NVARCHAR(MAX)")
    private String mentionedText;

    /** Comment do Groq AI tạo ra để trả lời */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_response_comment_id",
                foreignKey = @ForeignKey(name = "fk_ai_mentions_response"))
    private Comment aiResponseComment;

    /** false = chưa xử lý, true = Groq đã trả lời xong */
    @Column(nullable = false)
    @Builder.Default
    private Boolean processed = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}