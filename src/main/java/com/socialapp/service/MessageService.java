package com.socialapp.service;

import com.socialapp.dto.request.SendMessageRequest;
import com.socialapp.dto.response.ConversationResponse;
import com.socialapp.dto.response.MessageResponse;
import com.socialapp.dto.response.UserResponse;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository     messageRepository;
    private final UserRepository        userRepository;
    private final SimpMessagingTemplate messagingTemplate;

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
    // GET /api/messages  (danh sách hội thoại)
    // =============================================

    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations(UserDetails currentUser) {
        User me = getUser(currentUser.getUsername());

        List<Message> allMessages = messageRepository
                .findAllByUserIdOrderByCreatedAtDesc(me.getId());

        // Group theo "người đối thoại" — giữ tin mới nhất của mỗi người
        // LinkedHashMap giữ thứ tự insert = thứ tự mới nhất trước (vì query đã ORDER BY DESC)
        Map<Long, ConversationResponse> grouped = new LinkedHashMap<>();

        for (Message m : allMessages) {
            User other = m.getSender().getId().equals(me.getId())
                    ? m.getReceiver()
                    : m.getSender();

            // Chỉ giữ tin đầu tiên gặp (= mới nhất, vì list đã sort DESC)
            if (!grouped.containsKey(other.getId())) {
                long unread = messageRepository
                        .countByReceiverIdAndIsRead(me.getId(), false);
                // unread ở trên là tổng toàn bộ, cần đếm riêng theo otherUser:
                // dùng cách đơn giản — đếm trong list đã có sẵn
                long unreadFromOther = allMessages.stream()
                        .filter(msg -> msg.getSender().getId().equals(other.getId())
                                    && msg.getReceiver().getId().equals(me.getId())
                                    && !msg.getIsRead())
                        .count();

                grouped.put(other.getId(), new ConversationResponse(
                        UserResponse.from(other),
                        m.getContent(),
                        m.getCreatedAt(),
                        unreadFromOther
                ));
            }
        }

        return List.copyOf(grouped.values());
    }

    // =============================================
    // Push real-time
    // =============================================

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