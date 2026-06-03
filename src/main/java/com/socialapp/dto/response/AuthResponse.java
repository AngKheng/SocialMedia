package com.socialapp.dto.response;

import java.time.LocalDateTime;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UserResponse user
) {
    /** Factory method tiện lợi */
    public static AuthResponse of(String accessToken,
                                   String refreshToken,
                                   UserResponse user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", user);
    }
}