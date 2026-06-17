package com.socialapp.config;

import com.socialapp.security.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;   // ← thêm mới

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        /*
         * /topic  → broadcast (1 gửi, nhiều nhận)
         *           Dùng cho: notification chung
         * /queue  → point-to-point (1 gửi, 1 nhận)
         *           Dùng cho: chat 1-1, notification cá nhân
         */
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix cho message gửi từ client lên server (@MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix cho message gửi đến user cụ thể (convertAndSendToUser)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // Cho phép React dev server kết nối
                .setAllowedOriginPatterns("http://localhost:5173")
                // SockJS fallback cho browser không hỗ trợ native WebSocket
                .withSockJS();
    }

    /**
     * Gắn interceptor xác thực JWT vào kênh inbound (client → server).
     * Đây là nơi xử lý lệnh CONNECT trước khi Spring tạo session.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}