package com.socialapp.service;

import com.socialapp.model.AiMention;
import com.socialapp.repository.AiMentionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroqService {

 private final WebClient.Builder webClientBuilder;
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
 * @param postId post chứa @groq mention
 * @param userId user đang hỏi
 * @param newQuestion câu hỏi mới nhất (đã strip @groq)
 * @return text trả lời từ Groq
 */
@Transactional(readOnly = true)
 public String ask(Long postId, Long userId, String newQuestion) {
 List<Map<String, String>> messages = buildMessages(postId, userId, newQuestion);

 Map<String, Object> requestBody = Map.of(
 "model", model,
 "messages", messages,
 "max_tokens", maxTokens,
 "temperature", temperature
 );

 try {
 // Timeout 30s cho toàn bộ round-trip (đề phòng wire-level hang)
 Map<?, ?> response = webClientBuilder.build()
 .post()
 .uri(apiUrl)
 .header("Authorization", "Bearer " + apiKey)
 .header("Content-Type", "application/json")
 .bodyValue(requestBody)
 .retrieve()
 // Bắt lỗi HTTP 4xx/5xx với body chi tiết.
 // onStatus yêu cầu lambda return Mono<Throwable> — dùng
 // WebClientResponseException để truyền status + body xuống catch.
 .onStatus(HttpStatusCode::isError, clientResponse -> {
 log.error("Groq API trả về status {} cho post={} user={}",
 clientResponse.statusCode(), postId, userId);
 return clientResponse.bodyToMono(String.class)
 .defaultIfEmpty("")
 .map(body -> {
 log.error("Groq error body: {}", body);
 return new WebClientResponseException(
 clientResponse.statusCode().value(),
 "Groq API error",
 clientResponse.headers().asHttpHeaders(),
 body.getBytes(),
 null);
 });
 })
 .bodyToMono(Map.class)
 .block(Duration.ofSeconds(30));

 // Parse response: choices[0].message.content
 if (response != null) {
 List<?> choices = (List<?>) response.get("choices");
 if (choices != null && !choices.isEmpty()) {
 Map<?, ?> choice = (Map<?, ?>) choices.get(0);
 Map<?, ?> message = (Map<?, ?>) choice.get("message");
 if (message != null) {
 String content = (String) message.get("content");
 if (content != null && !content.isBlank()) {
 return content;
 }
 }
 }
 }

 log.error("Groq response rỗng hoặc thiếu choices cho post={} user={}",
 postId, userId);
 throw new RuntimeException("Groq trả về response rỗng");

 } catch (WebClientResponseException e) {
 // Lỗi HTTP 4xx/5xx có body
 log.error("Groq HTTP {} cho post={} user={}: {}",
 e.getStatusCode(), postId, userId, e.getResponseBodyAsString());
 throw new RuntimeException(
 "Groq API lỗi " + e.getStatusCode() + ", xem log backend");
 } catch (Exception e) {
 log.error("Groq API error (không xác định được HTTP status): {}",
 e.getMessage(), e);
 throw new RuntimeException("Groq AI tạm thời không khả dụng, thử lại sau");
 }
 }

 // =============================================
 // Build conversation history
 // =============================================

 /**
 * Xây dựng danh sách messages gửi lên Groq:
 * my guidelines + lịch sử hội thoại cũ + câu hỏi mới.
 */
 private List<Map<String, String>> buildMessages(Long postId, Long userId,
                                                 String newQuestion) {
    List<Map<String, String>> messages = new ArrayList<>();
    messages.add(Map.of("role", "system", "content", systemPrompt));

    // ✅ Dùng query JOIN FETCH mới
    List<AiMention> history = aiMentionRepository
            .findHistoryWithResponse(postId, userId);  // ← đổi tên

    for (AiMention mention : history) {
        if (mention.getMentionedText() != null) {
            messages.add(Map.of("role", "user", "content", mention.getMentionedText()));
        }
        // ✅ Không còn LazyInitializationException nữa
        if (mention.getAiResponseComment() != null) {
            messages.add(Map.of("role", "assistant",
                    "content", mention.getAiResponseComment().getContent()));
        }
    }

    messages.add(Map.of("role", "user", "content", newQuestion));
    return messages;
}
}
