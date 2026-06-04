package com.socialapp.dto.response;

import com.socialapp.model.User;

import java.time.LocalDateTime;

/**
 * Response đầy đủ cho trang profile:
 * bao gồm số follower / following + isFollowing (xem bởi người khác).
 */
public record UserProfileResponse(
        Long id,
        String username,
        String displayName,
        String avatarUrl,
        String bio,
        Boolean isBot,
        LocalDateTime createdAt,
        long followerCount,
        long followingCount,
        boolean isFollowing   // người đang đăng nhập có follow user này không
) {
    public static UserProfileResponse from(User user,
                                           long followerCount,
                                           long followingCount,
                                           boolean isFollowing) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getIsBot(),
                user.getCreatedAt(),
                followerCount,
                followingCount,
                isFollowing
        );
    }
}