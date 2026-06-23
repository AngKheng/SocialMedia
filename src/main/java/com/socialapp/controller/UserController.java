package com.socialapp.controller;

import com.socialapp.dto.request.ChangePasswordRequest;
import com.socialapp.dto.request.UpdateProfileRequest;
import com.socialapp.dto.response.UserProfileResponse;
import com.socialapp.dto.response.UserResponse;
import com.socialapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

 private final UserService userService;

 /**
 * GET /api/users/{id}
 * Xem profile của bất kỳ user nào.
 * Trả về follower/following count + isFollowing.
 */
 @GetMapping("/{id}")
 public ResponseEntity<UserProfileResponse> getProfile(
 @PathVariable Long id,
 @AuthenticationPrincipal UserDetails currentUser) {

 return ResponseEntity.ok(userService.getProfile(id, currentUser));
 }

 /**
 * PUT /api/users/me
 * Cập nhật profile của chính mình.
 * Chỉ gửi field nào muốn đổi, field null sẽ giữ nguyên.
 */
 @PutMapping("/me")
 public ResponseEntity<UserResponse> updateProfile(
 @Valid @RequestBody UpdateProfileRequest request,
 @AuthenticationPrincipal UserDetails currentUser) {

 return ResponseEntity.ok(userService.updateProfile(request, currentUser));
 }

 /**
 * PUT /api/users/me/password
 * Đổi mật khẩu của chính mình.
 * Yêu cầu gửi currentPassword (verify) + newPassword + confirmPassword.
 */
 @PutMapping("/me/password")
 public ResponseEntity<Void> changePassword(
 @Valid @RequestBody ChangePasswordRequest request,
 @AuthenticationPrincipal UserDetails currentUser) {

 userService.changePassword(request, currentUser);
 return ResponseEntity.ok().build();
 }
}
