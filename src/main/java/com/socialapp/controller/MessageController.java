package com.socialapp.controller;

import com.socialapp.dto.request.SendMessageRequest;
import com.socialapp.dto.response.ConversationResponse;
import com.socialapp.dto.response.MessageResponse;
import com.socialapp.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * GET /api/messages
     * Danh sách hội thoại — mỗi người đối thoại 1 dòng, mới nhất trước.
     * Dùng để hiển thị sidebar danh sách chat.
     */
    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getConversations(
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(messageService.getConversations(currentUser));
    }

    /**
     * POST /api/messages
     * Gửi tin nhắn.
     */
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(messageService.sendMessage(request, currentUser));
    }

    /**
     * GET /api/messages/{userId}
     * Lấy toàn bộ lịch sử chat với userId.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(messageService.getConversation(userId, currentUser));
    }
}