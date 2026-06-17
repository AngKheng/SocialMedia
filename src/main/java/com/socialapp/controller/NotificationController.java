package com.socialapp.controller;

import com.socialapp.dto.response.NotificationResponse;
import com.socialapp.dto.response.PageResponse;
import com.socialapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/notifications?page=0&size=20
     * Danh sách thông báo của người đang đăng nhập, mới nhất trước.
     */
    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(
                notificationService.getNotifications(page, size, currentUser));
    }

    /**
     * GET /api/notifications/unread-count
     * Số thông báo chưa đọc — dùng để hiển thị badge trên UI.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails currentUser) {

        long count = notificationService.getUnreadCount(currentUser);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * PUT /api/notifications/read
     * Đánh dấu tất cả thông báo là đã đọc.
     */
    @PutMapping("/read")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetails currentUser) {

        notificationService.markAllAsRead(currentUser);
        return ResponseEntity.noContent().build();
    }
}