package com.socialapp.dto.response;

import com.socialapp.model.User;

/**
 * DTO người dùng nhúng trong PostResponse.
 * Giống UserResponse nhưng có thêm isFollowing —
 * để frontend biết trạng thái Follow/Unfollow ngay lúc load feed,
 * không cần gọi thêm API riêng cho từng bài.
 */
public record PostUserResponse(
        Long id,
        String username,
        String displayName,
        String avatarUrl,
        Boolean isBot,
        boolean isFollowing  // người đang đăng nhập có follow tác giả bài này không
) {
    public static PostUserResponse from(User user, boolean isFollowing) {
        return new PostUserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getIsBot(),
                isFollowing
        );
    }
}