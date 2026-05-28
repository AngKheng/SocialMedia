package com.socialapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_comments_post"))
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_comments_user"))
    private User user;

    @Column(nullable = false, length = 1000)
    private String content;

    /**
     * null  → comment gốc
     * !null → reply của comment khác
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id",
                foreignKey = @ForeignKey(name = "fk_comments_parent"))
    private Comment parentComment;

    /**
     * true  → comment này do Groq AI tạo ra
     * false → comment của người dùng thường
     */
    @Column(name = "is_ai_response", nullable = false)
    @Builder.Default
    private Boolean isAiResponse = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}