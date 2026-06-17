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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository     messageRepository;
    private final UserRepository        userRepository;
    private final SimpMessagingTemplate messagingTemplate;   // ← thêm mới

    private static final String CHAT_DESTINATION = "/queue/messages";

    // =============================================
    // POST /api/messages  (dùng chung cho REST lẫn WebSocket)
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

        MessageResponse response = MessageResponse.from(saved);

        // Push real-time tới người nhận (nếu đang online)
        pushRealtime(receiver, response);

        return response;
    }

    // =============================================
    // GET /api/messages/{userId}
    // =============================================

    @Transactional
    public List<MessageResponse> getConversation(Long otherUserId, UserDetails currentUser) {
        User me = getUser(currentUser.getUsername());
        getUserById(otherUserId);

        List<Message> messages = messageRepository.findConversation(me.getId(), otherUserId);
        messageRepository.markConversationAsRead(me.getId(), otherUserId);

        return messages.stream()
                .map(MessageResponse::from)
                .toList();
    }

    // =============================================
    // Push real-time
    // =============================================

    /**
     * Gửi tin nhắn qua WebSocket tới đúng người nhận đang online.
     * Nếu không online, message bị bỏ qua — REST API vẫn là nguồn chính,
     * người nhận sẽ thấy tin nhắn khi load lại lịch sử chat.
     */
    private void pushRealtime(User receiver, MessageResponse message) {
        try {
            messagingTemplate.convertAndSendToUser(
                    receiver.getUsername(),
                    CHAT_DESTINATION,
                    message
            );
            log.debug("Đã push WS message tới @{}", receiver.getUsername());
        } catch (Exception e) {
            log.warn("Push WS message thất bại: {}", e.getMessage());
        }
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