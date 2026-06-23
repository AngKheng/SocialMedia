package com.socialapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracking user online/offline qua WebSocket sessions.
 *
 * Lưu in-memory (ConcurrentHashMap) vì:
 * - Không cần persist sau restart server
 * - Đủ nhanh cho app quy mô hiện tại
 * - Nếu cần scale → có thể thay bằng Redis sau
 *
 * userId → Set<sessionId>: 1 user có thể mở nhiều tab → nhiều sessionId.
 * Chỉ khi Set rỗng → mới tính là offline.
 */
@Service
@Slf4j
public class PresenceService {

 /** Map userId → tập các sessionId đang active */
 private final ConcurrentHashMap<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

 /**
 * Đánh dấu user online (userId + sessionId mới).
 * Nếu sessionId đã tồn tại → bỏ qua (idempotent).
 */
 public void connect(Long userId, String sessionId) {
 userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
 .add(sessionId);
 log.debug("User {} online (session={}), tổng session={}",
 userId, sessionId, userSessions.get(userId).size());
 }

 /**
 * Đánh dấu user offline (xóa sessionId).
 * Nếu Set rỗng → xóa luôn entry khỏi map.
 * Trả về true nếu user vừa chuyển từ online → offline (Set rỗng sau khi xóa).
 */
 public boolean disconnect(Long userId, String sessionId) {
 Set<String> sessions = userSessions.get(userId);
 if (sessions == null) return false;

 sessions.remove(sessionId);
 boolean wasOnline = !sessions.isEmpty();

 if (sessions.isEmpty()) {
 userSessions.remove(userId);
 log.debug("User {} offline (hết session)", userId);
 } else {
 log.debug("User {} vẫn online (còn {} session)", userId, sessions.size());
 }

 // Trả về true = user vừa chuyển offline (để broadcast)
 return !wasOnline && sessions.isEmpty();
 }

 /** Kiểm tra user có đang online không */
 public boolean isOnline(Long userId) {
 Set<String> sessions = userSessions.get(userId);
 return sessions != null && !sessions.isEmpty();
 }

 /** Lấy tập userId đang online (dùng cho broadcast) */
 public Set<Long> getOnlineUserIds() {
 return Collections.unmodifiableSet(userSessions.keySet());
 }
}