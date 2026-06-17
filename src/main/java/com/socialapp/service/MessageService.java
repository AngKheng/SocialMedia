package com.socialapp.service;

import com.socialapp.dto.request.SendMessageRequest;
import com.socialapp.dto.response.MessageResponse;
import com.socialapp.exception.ResourceNotFoundException;
import com.socialapp.model.Message;
import com.socialapp.model.User;
import com.socialapp.repository.MessageRepository;
import com.socialapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository    userRepository;

    // =============================================
    // POST /api/messages
    // =============================================

    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request, UserDetails currentUser) {
        User sender   = getUser(currentUser.getUsername());
        User receiver = getUserById(request.receiverId());

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Không thể gửi tin nhắn cho chính mình");
        }

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.content())
                .isRead(false)
                .build();

        Message saved = messageRepository.save(message);
        log.info("@{} gửi tin nhắn tới @{}", sender.getUsername(), receiver.getUsername());

        return MessageResponse.from(saved);
    }

    // =============================================
    // GET /api/messages/{userId}
    // =============================================

    @Transactional
    public List<MessageResponse> getConversation(Long otherUserId, UserDetails currentUser) {
        User me = getUser(currentUser.getUsername());
        getUserById(otherUserId); // 404 nếu không tồn tại

        List<Message> messages = messageRepository
                .findConversation(me.getId(), otherUserId);

        // Đánh dấu đã đọc các tin gửi từ otherUserId tới mình
        messageRepository.markConversationAsRead(me.getId(), otherUserId);

        return messages.stream()
                .map(MessageResponse::from)
                .toList();
    }

    // =============================================
    // Helper
    // =============================================

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User không tồn tại: " + username));
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}