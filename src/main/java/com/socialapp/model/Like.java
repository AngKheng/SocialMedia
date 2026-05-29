package com.socialapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "likes",
    uniqueConstraints = {
        // Mỗi user chỉ like một post một lần
        @UniqueConstraint(name = "uk_likes_user_post",
                          columnNames = {"user_id", "post_id"}),
        // Mỗi user chỉ like một comment một lần
        @UniqueConstraint(name = "uk_likes_user_comment",
                          columnNames = {"user_id", "comment_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_likes_user"))
    private User user;

    /**
     * Like bài post — nullable vì có thể like comment.
     * Constraint DB: post_id và comment_id không cùng null/not-null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id",
                foreignKey = @ForeignKey(name = "fk_likes_post"))
    private Post post;

    /**
     * Like comment — nullable vì có thể like post.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id",
                foreignKey = @ForeignKey(name = "fk_likes_comment"))
    private Comment comment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}