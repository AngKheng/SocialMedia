package com.socialapp.service;

import com.socialapp.model.AiMention;
import com.socialapp.repository.AiMentionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroqService {

    private final WebClient.Builder  webClientBuilder;
    private final AiMentionRepository aiMentionRepository;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.model}")
    private String model;

    @Value("${groq.api.max-tokens}")
    private int maxTokens;

    @Value("${groq.api.temperature}")
    private double temperature;

    @Value("${groq.bot.system-prompt}")
    private String systemPrompt;

    /**
     * Gọi Groq API với full context hội thoại trước đó.
     *
     * @param postId     post chứa @groq mention
     * @param userId     user đang hỏi
     * @param newQuestion câu hỏi mới nhất (đã strip @groq)
     * @return text trả lời từ Groq
     */
    public String ask(Long postId, Long userId, String newQuestion) {
        List<Map<String, String>> messages = buildMessages(postId, userId, newQuestion);

        Map<String, Object> requestBody = Map.of(
                "model",       model,
                "messages",    messages,
                "max_tokens",  maxTokens,
                "temperature", temperature
        );

        try {
            Map<?, ?> response = webClientBuilder.build()
                    .post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // Parse response: choices[0].message.content
            if (response != null) {
                List<?> choices = (List<?>) response.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<?, ?> choice  = (Map<?, ?>) choices.get(0);
                    Map<?, ?> message = (Map<?, ?>) choice.get("message");
                    if (message != null) {
                        return (String) message.get("content");
                    }
                }
            }

            throw new RuntimeException("Groq trả về response rỗng");

        } catch (Exception e) {
            log.error("Groq API error: {}", e.getMessage());
            throw new RuntimeException("Groq AI tạm thời không khả dụng, thử lại sau");
        }
    }

    // =============================================
    // Build conversation history
    // =============================================

    /**
     * Xây dựng danh sách messages gửi lên Groq:
     * system prompt + lịch sử hội thoại cũ + câu hỏi mới.
     */
    private List<Map<String, String>> buildMessages(Long postId, Long userId,
                                                     String newQuestion) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 1. System prompt
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // 2. Lịch sử hội thoại cũ của user này trong post này
        List<AiMention> history = aiMentionRepository
                .findByPostIdAndUserIdAndProcessedTrueOrderByCreatedAtAsc(postId, userId);

        for (AiMention mention : history) {
            // Câu hỏi của user
            if (mention.getMentionedText() != null) {
                messages.add(Map.of("role", "user", "content", mention.getMentionedText()));
            }
            // Câu trả lời của AI
            if (mention.getAiResponseComment() != null) {
                messages.add(Map.of("role", "assistant",
                        "content", mention.getAiResponseComment().getContent()));
            }
        }

        // 3. Câu hỏi mới nhất
        messages.add(Map.of("role", "user", "content", newQuestion));

        return messages;
    }
}