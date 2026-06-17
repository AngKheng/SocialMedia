package com.socialapp.dto.response;

import com.socialapp.model.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        UserResponse actor,       // người thực hiện hành động (like, follow, comment...)
        String type,              // LIKE, COMMENT, FOLLOW, MENTION, GROQ_REPLY, NEW_MESSAGE
        Long postId,              // post liên quan (nếu có)
        Long commentId,           // comment liên quan (nếu có)
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getActor() != null ? UserResponse.from(n.getActor()) : null,
                n.getType().name(),
                n.getPost()    != null ? n.getPost().getId()    : null,
                n.getComment() != null ? n.getComment().getId() : null,
                n.getIsRead(),
                n.getCreatedAt()
        );
    }
}