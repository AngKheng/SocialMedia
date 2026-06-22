package com.socialapp.dto.response;

import com.socialapp.model.Post;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record PostResponse(
        Long id,
        PostUserResponse user,   // dùng PostUserResponse để có isFollowing
        String content,
        List<String> mediaUrls,
        boolean isLiked,
        boolean isRepost,
        Long originalPostId,
        int likeCount,
        int commentCount,
        int repostCount,
        LocalDateTime createdAt
) {
    /** Dùng khi KHÔNG biết trạng thái like/follow (không có currentUser) */
    public static PostResponse from(Post post) {
        return from(post, false, false);
    }

    /** Dùng khi biết isLiked nhưng chưa biết isFollowing */
    public static PostResponse from(Post post, boolean isLiked) {
        return from(post, isLiked, false);
    }

    /** Full — dùng ở Feed/PostDetail khi có currentUser để check cả like lẫn follow */
    public static PostResponse from(Post post, boolean isLiked, boolean isFollowing) {
        List<String> urls = (post.getImageUrls() != null && !post.getImageUrls().isBlank())
                ? Arrays.asList(post.getImageUrls().split(","))
                : Collections.emptyList();

        return new PostResponse(
                post.getId(),
                PostUserResponse.from(post.getUser(), isFollowing),
                post.getContent(),
                urls,
                isLiked,
                post.getIsRepost(),
                post.getOriginalPost() != null ? post.getOriginalPost().getId() : null,
                post.getLikeCount(),
                post.getCommentCount(),
                post.getRepostCount(),
                post.getCreatedAt()
        );
    }
}