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
        List<String> mediaUrls,   // tách từ imageUrls (comma-separated) thành List
        boolean isRepost,
        Long originalPostId,
        int likeCount,
        int commentCount,
        int repostCount,
        LocalDateTime createdAt
) {
    public static PostResponse from(Post post) {
        // Tách chuỗi "url1,url2" thành List
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
                post.getCreatedAt()
        );
    }
}