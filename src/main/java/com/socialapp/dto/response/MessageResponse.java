package com.socialapp.dto.response;

import com.socialapp.model.Message;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long senderId,
        Long receiverId,
        String content,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static MessageResponse from(Message m) {
        return new MessageResponse(
                m.getId(),
                m.getSender().getId(),
                m.getReceiver().getId(),
                m.getContent(),
                m.getIsRead(),
                m.getCreatedAt()
        );
    }
}