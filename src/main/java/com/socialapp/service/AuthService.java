package com.socialapp.service;

import com.socialapp.dto.request.LoginRequest;
import com.socialapp.dto.request.RefreshTokenRequest;
import com.socialapp.dto.request.RegisterRequest;
import com.socialapp.dto.response.AuthResponse;
import com.socialapp.dto.response.UserResponse;
import com.socialapp.exception.DuplicateResourceException;
import com.socialapp.exception.ResourceNotFoundException;
import com.socialapp.model.User;
import com.socialapp.repository.UserRepository;
import com.socialapp.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

 private final UserRepository userRepository;
 private final PasswordEncoder passwordEncoder;
 private final JwtUtil jwtUtil;
 private final AuthenticationManager authenticationManager;
 private final UserDetailsService userDetailsService;

 @Transactional
 public AuthResponse register(RegisterRequest request) {
 if (userRepository.existsByUsername(request.username())) {
 throw new DuplicateResourceException(
 "Username '" + request.username() + "' đã được sử dụng");
 }
 if (userRepository.existsByEmail(request.email())) {
 throw new DuplicateResourceException(
 "Email '" + request.email() + "' đã được sử dụng");
 }

 User user = User.builder()
 .username(request.username())
 .email(request.email())
 .passwordHash(passwordEncoder.encode(request.password()))
 .displayName(request.displayName() != null
 ? request.displayName()
 : request.username())
 .isBot(false)
 .isActive(true)
 .build();

 userRepository.save(user);
 log.info("Đăng ký thành công: @{}", user.getUsername());
 return buildAuthResponse(user.getUsername(), user);
 }

 public AuthResponse login(LoginRequest request) {
 authenticationManager.authenticate(
 new UsernamePasswordAuthenticationToken(
 request.username(), request.password()));

 User user = userRepository.findByUsername(request.username())
 .orElseThrow(() -> new RuntimeException("User không tồn tại"));

 log.info("Đăng nhập thành công: @{}", user.getUsername());
 return buildAuthResponse(user.getUsername(), user);
 }

 /**
 * Phase 9J: Refresh token flow.
 *
 * Verify refreshToken (phải có type=refresh + còn hạn + user còn active).
 * Trả về accessToken + refreshToken MỚI (xoay vòng, không dùng lại refreshToken cũ).
 */
 public AuthResponse refresh(RefreshTokenRequest request) {
 String refreshToken = request.refreshToken();

 // 1. Verify token còn hạn + là refresh token
 if (!jwtUtil.isTokenValid(refreshToken, null)) {
 throw new IllegalArgumentException("Refresh token không hợp lệ hoặc đã hết hạn");
 }

 String tokenType = jwtUtil.extractTokenType(refreshToken);
 if (!"refresh".equals(tokenType)) {
 throw new IllegalArgumentException("Token không phải refresh token");
 }

 // 2. Verify user còn active + load UserDetails
 String username = jwtUtil.extractUsername(refreshToken);
 UserDetails userDetails = userDetailsService.loadUserByUsername(username);

 // Verify lại lần nữa với userDetails để chắc chắn username khớp
 if (!jwtUtil.isTokenValid(refreshToken, userDetails)) {
 throw new IllegalArgumentException("Refresh token không hợp lệ");
 }

 User user = userRepository.findByUsername(username)
 .orElseThrow(() -> new ResourceNotFoundException(
 "User không tồn tại: " + username));

 if (Boolean.FALSE.equals(user.getIsActive())) {
 throw new IllegalArgumentException("Tài khoản đã bị khóa");
 }

 // 3. Generate cặp token mới (xoay vòng refreshToken)
 log.info("Refresh token thành công: @{}", username);
 return buildAuthResponse(username, user);
 }

 private AuthResponse buildAuthResponse(String username, User user) {
 UserDetails userDetails = userDetailsService.loadUserByUsername(username);
 String accessToken = jwtUtil.generateAccessToken(userDetails);
 String refreshToken = jwtUtil.generateRefreshToken(userDetails);
 return AuthResponse.of(accessToken, refreshToken, UserResponse.from(user));
 }
}