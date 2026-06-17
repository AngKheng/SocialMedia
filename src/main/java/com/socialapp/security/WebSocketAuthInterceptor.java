package com.socialapp.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * Đọc JWT từ header "Authorization" của lệnh STOMP CONNECT,
 * xác thực rồi gắn Principal vào session WebSocket.
 *
 * Sau bước này, convertAndSendToUser(username, ...) mới hoạt động đúng,
 * vì Spring cần biết session nào tương ứng với username nào.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final com.socialapp.security.JwtUtil jwtUtil;
    private final UserDetailsService             userDetailsService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    String username = jwtUtil.extractUsername(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.isTokenValid(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());

                        accessor.setUser(authToken);
                        log.info("WebSocket xác thực thành công: @{}", username);
                    } else {
                        log.warn("WebSocket token không hợp lệ hoặc đã hết hạn");
                    }
                } catch (Exception e) {
                    log.warn("WebSocket xác thực thất bại: {}", e.getMessage());
                }
            } else {
                log.warn("WebSocket CONNECT thiếu Authorization header");
            }
        }

        return message;
    }
}