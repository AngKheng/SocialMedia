package com.socialapp.dto.response;

import com.socialapp.model.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        String displayName,
        String avatarUrl,
        String bio,
        Boolean isBot,
        LocalDateTime createdAt
) {
    /** Chuyển từ Entity sang DTO */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getIsBot(),
                user.getCreatedAt()
        );
    }
}