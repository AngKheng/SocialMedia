package com.socialapp.dto.response;

import com.socialapp.model.Post;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record PostResponse(
        Long id,
        UserResponse user,
        String content,
        List<String> mediaUrls,
        boolean isRepost,
        Long originalPostId,
        int likeCount,
        int commentCount,
        int repostCount,
        boolean isLiked,    // ← thêm mới: người đang xem có like bài này chưa
        LocalDateTime createdAt
) {
    /**
     * Dùng khi không cần biết trạng thái like (ví dụ: bối cảnh không có currentUser).
     * isLiked mặc định = false.
     */
    public static PostResponse from(Post post) {
        return from(post, false);
    }

    /**
     * Dùng khi đã biết currentUser có like bài này hay chưa.
     */
    public static PostResponse from(Post post, boolean isLiked) {
        List<String> urls = (post.getImageUrls() != null && !post.getImageUrls().isBlank())
                ? Arrays.asList(post.getImageUrls().split(","))
                : Collections.emptyList();

        return new PostResponse(
                post.getId(),
                UserResponse.from(post.getUser()),
                post.getContent(),
                urls,
                post.getIsRepost(),
                post.getOriginalPost() != null ? post.getOriginalPost().getId() : null,
                post.getLikeCount(),
                post.getCommentCount(),
                post.getRepostCount(),
                isLiked,
                post.getCreatedAt()
        );
    }
}