package com.socialapp.controller;

import com.socialapp.dto.request.SendMessageRequest;
import com.socialapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

/**
 * Nhận tin nhắn gửi trực tiếp qua WebSocket (STOMP),
 * khác với MessageController (REST) ở chỗ:
 * - Không trả response HTTP, mà push qua /queue/messages cho người nhận
 * - Người gửi cũng có thể subscribe /user/queue/messages để nhận lại bản thân
 *   (echo) nếu cần đồng bộ nhiều tab/device.
 *
 * Client gửi tới: /app/chat.send
 * Body: { "receiverId": 2, "content": "Hello" }
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageWsController {

    private final MessageService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request,
                            Authentication authentication) {

        if (authentication == null) {
            log.warn("WS chat.send bị từ chối: chưa xác thực");
            return;
        }

        UserDetails currentUser = (UserDetails) authentication.getPrincipal();

        // Service đã tự lưu DB + push WS tới người nhận
        messageService.sendMessage(request, currentUser);
    }
}