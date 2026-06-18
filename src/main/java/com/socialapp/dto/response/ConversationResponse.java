package com.socialapp.dto.response;

import java.time.LocalDateTime;

/**
 * 1 dòng trong danh sách hội thoại — đại diện cho cuộc chat với 1 người khác.
 */
public record ConversationResponse(
        UserResponse otherUser,
        String lastMessage,
        LocalDateTime lastMessageAt,
        long unreadCount
) {}