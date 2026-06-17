package com.socialapp.service;

import com.socialapp.dto.response.NotificationResponse;
import com.socialapp.dto.response.PageResponse;
import com.socialapp.exception.ResourceNotFoundException;
import com.socialapp.model.Comment;
import com.socialapp.model.Notification;
import com.socialapp.model.Notification.Type;
import com.socialapp.model.Post;
import com.socialapp.model.User;
import com.socialapp.repository.NotificationRepository;
import com.socialapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository         userRepository;

    // =============================================
    // GET /api/notifications?page=0&size=20
    // =============================================

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(int page, int size,
                                                                UserDetails currentUser) {
        User me = getUser(currentUser.getUsername());
        return PageResponse.from(
                notificationRepository
                        .findByUserIdOrderByCreatedAtDesc(me.getId(), PageRequest.of(page, size))
                        .map(NotificationResponse::from)
        );
    }

    // =============================================
    // GET /api/notifications/unread-count
    // =============================================

    @Transactional(readOnly = true)
    public long getUnreadCount(UserDetails currentUser) {
        User me = getUser(currentUser.getUsername());
        return notificationRepository.countByUserIdAndIsRead(me.getId(), false);
    }

    // =============================================
    // PUT /api/notifications/read
    // =============================================

    @Transactional
    public void markAllAsRead(UserDetails currentUser) {
        User me = getUser(currentUser.getUsername());
        notificationRepository.markAllAsRead(me.getId());
        log.info("@{} đã đọc tất cả thông báo", me.getUsername());
    }

    // =============================================
    // Tạo thông báo — gọi từ các service khác (async)
    // =============================================

    /** Khi có người like bài */
    @Async
    @Transactional
    public void notifyLikePost(User actor, Post post) {
        // Không tự thông báo cho chính mình
        if (actor.getId().equals(post.getUser().getId())) return;
        save(post.getUser(), actor, Type.LIKE, post, null);
    }

    /** Khi có người comment vào bài */
    @Async
    @Transactional
    public void notifyComment(User actor, Post post, Comment comment) {
        if (actor.getId().equals(post.getUser().getId())) return;
        save(post.getUser(), actor, Type.COMMENT, post, comment);
    }

    /** Khi có người follow */
    @Async
    @Transactional
    public void notifyFollow(User actor, User target) {
        if (actor.getId().equals(target.getId())) return;
        save(target, actor, Type.FOLLOW, null, null);
    }

    /** Khi Groq AI reply */
    @Async
    @Transactional
    public void notifyGroqReply(User recipient, Post post, Comment aiComment) {
        User groqBot = userRepository.findByUsernameAndIsBot("groq", true)
                .orElse(null);
        save(recipient, groqBot, Type.GROQ_REPLY, post, aiComment);
    }

    // =============================================
    // Helper
    // =============================================

    private void save(User recipient, User actor, Type type, Post post, Comment comment) {
        try {
            notificationRepository.save(Notification.builder()
                    .user(recipient)
                    .actor(actor)
                    .type(type)
                    .post(post)
                    .comment(comment)
                    .build());
        } catch (Exception e) {
            log.error("Lưu notification thất bại: {}", e.getMessage());
        }
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User không tồn tại: " + username));
    }
}