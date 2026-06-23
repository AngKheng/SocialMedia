package com.socialapp.config;

import com.socialapp.model.User;
import com.socialapp.repository.UserRepository;
import com.socialapp.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Lắng nghe WebSocket session lifecycle events của Spring.
 *
 * - CONNECTED → mark online + broadcast "userId came online" cho người đang chat với user đó
 * - DISCONNECTED → mark offline + broadcast "userId went offline"
 *
 * Broadcast qua /queue/presence đến các user đang trong conversation với user vừa đổi trạng thái.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

 private final PresenceService presenceService;
 private final UserRepository userRepository;
 private final SimpMessagingTemplate messagingTemplate;

 private static final String PRESENCE_DESTINATION = "/queue/presence";

 /**
 * Khi client STOMP CONNECT thành công → đánh dấu online.
 * Lấy username từ Principal đã được WebSocketAuthInterceptor set.
 */
 @EventListener
 public void handleSessionConnected(SessionConnectedEvent event) {
 StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
 String sessionId = accessor.getSessionId();
 String username = extractUsername(accessor);

 if (username == null) return;

 Optional<User> userOpt = userRepository.findByUsername(username);
 if (userOpt.isEmpty()) return;

 Long userId = userOpt.get().getId();
 presenceService.connect(userId, sessionId);
 log.info("WS CONNECTED: @{} (session={})", username, sessionId);

 // Broadcast cho những user đang chat với user này
 broadcastPresence(userId, true);
 }

 /**
 * Khi client disconnect (đóng tab, logout, mất mạng) → đánh dấu offline.
 * Spring tự động publish event này cho cả STOMP disconnect và SockJS close.
 */
 @EventListener
 @Transactional(readOnly = true)
 public void handleSessionDisconnect(SessionDisconnectEvent event) {
 StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
 String sessionId = accessor.getSessionId();
 String username = extractUsername(accessor);

 if (username == null) return;

 Optional<User> userOpt = userRepository.findByUsername(username);
 if (userOpt.isEmpty()) return;

 Long userId = userOpt.get().getId();
 boolean wentOffline = presenceService.disconnect(userId, sessionId);
 log.info("WS DISCONNECTED: @{} (session={})", username, sessionId);

 // Chỉ broadcast khi user vừa chuyển từ online → offline (hết session)
 if (wentOffline) {
 broadcastPresence(userId, false);
 }
 }

 /**
 * Trích username từ Principal (đã được set bởi WebSocketAuthInterceptor ở CONNECT).
 * Ưu tiên: Principal name → simpUser header → fallback null.
 */
 private String extractUsername(StompHeaderAccessor accessor) {
 if (accessor.getUser() != null) {
 return accessor.getUser().getName();
 }
 // Trường hợp SockJS: Principal có thể chưa được set ngay lúc disconnect
 String simpUser = accessor.getFirstNativeHeader("simpUser");
 return simpUser;
 }

 /**
 * Gửi presence update tới các user đang có conversation với userId.
 *
 * Đơn giản hoá: broadcast cho tất cả user đang online (in-memory set).
 * Khi scale lên, nên track riêng "ai đang chat với ai" thay vì broadcast all.
 */
 private void broadcastPresence(Long userId, boolean isOnline) {
 Map<String, Object> payload = new LinkedHashMap<>();
 payload.put("userId", userId);
 payload.put("isOnline", isOnline);

 // Gửi tới chính user đó (để các tab khác của user đó cũng cập nhật)
 Optional<User> userOpt = userRepository.findById(userId);
 userOpt.ifPresent(user -> messagingTemplate.convertAndSendToUser(
 user.getUsername(),
 PRESENCE_DESTINATION,
 payload
 ));

 log.debug("Broadcast presence: userId={} isOnline={}", userId, isOnline);
 }
}