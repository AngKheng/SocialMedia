package com.socialapp.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request đổi access token bằng refresh token.
 * Body: { refreshToken: "..." }
 *
 * Khi accessToken hết hạn, frontend gọi endpoint này với refreshToken
 * để lấy accessToken mới (không cần user đăng nhập lại).
 */
public record RefreshTokenRequest(

 @NotBlank(message = "Refresh token không được để trống")
 String refreshToken
) {}