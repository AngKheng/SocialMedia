package com.socialapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_posts_user"))
    private User user;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    /** Lưu nhiều URL ảnh, phân cách bằng dấu phẩy */
    @Column(name = "image_urls", columnDefinition = "NVARCHAR(MAX)")
    private String imageUrls;

    /** true = bài repost từ bài khác */
    @Column(name = "is_repost", nullable = false)
    @Builder.Default
    private Boolean isRepost = false;

    /** Bài gốc nếu đây là repost */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_post_id",
                foreignKey = @ForeignKey(name = "fk_posts_original"))
    private Post originalPost;

    /** Đếm nhanh, cập nhật khi có like/comment/repost */
    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private Integer commentCount = 0;

    @Column(name = "repost_count", nullable = false)
    @Builder.Default
    private Integer repostCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}