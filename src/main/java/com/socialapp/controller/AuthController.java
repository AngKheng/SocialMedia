package com.socialapp.controller;

import com.socialapp.dto.request.LoginRequest;
import com.socialapp.dto.request.RefreshTokenRequest;
import com.socialapp.dto.request.RegisterRequest;
import com.socialapp.dto.response.AuthResponse;
import com.socialapp.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

 private final AuthService authService;

 /**
 * POST /api/auth/register
 * Body: { username, email, password, displayName }
 * Response 201: { accessToken, refreshToken, tokenType, user }
 */
 @PostMapping("/register")
 public ResponseEntity<AuthResponse> register(
 @Valid @RequestBody RegisterRequest request) {

 AuthResponse response = authService.register(request);
 return ResponseEntity.status(HttpStatus.CREATED).body(response);
 }

 /**
 * POST /api/auth/login
 * Body: { username, password }
 * Response 200: { accessToken, refreshToken, tokenType, user }
 */
 @PostMapping("/login")
 public ResponseEntity<AuthResponse> login(
 @Valid @RequestBody LoginRequest request) {

 AuthResponse response = authService.login(request);
 return ResponseEntity.ok(response);
 }

 /**
 * POST /api/auth/refresh (Phase 9J)
 * Body: { refreshToken: "..." }
 * Response 200: { accessToken, refreshToken, tokenType, user }
 *
 * Đổi accessToken mới bằng refreshToken (không cần user đăng nhập lại).
 * Refresh token cũ hết hạn được thay bằng refresh token mới (xoay vòng).
 */
 @PostMapping("/refresh")
 public ResponseEntity<AuthResponse> refresh(
 @Valid @RequestBody RefreshTokenRequest request) {

 AuthResponse response = authService.refresh(request);
 return ResponseEntity.ok(response);
 }
}