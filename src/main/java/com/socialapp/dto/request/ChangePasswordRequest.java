package com.socialapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request đổi mật khẩu của user đang đăng nhập.
 * Cần cả currentPassword (để xác nhận) lẫn confirmPassword (tránh gõ sai).
 */
public record ChangePasswordRequest(

 @NotBlank(message = "Mật khẩu hiện tại không được để trống")
 String currentPassword,

 @NotBlank(message = "Mật khẩu mới không được để trống")
 @Size(min = 6, max = 100, message = "Mật khẩu mới phải từ 6-100 ký tự")
 String newPassword,

 @NotBlank(message = "Xác nhận mật khẩu không được để trống")
 String confirmPassword
) {}
