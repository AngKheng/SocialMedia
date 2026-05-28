package com.socialapp.config;

import com.socialapp.model.User;
import com.socialapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${groq.bot.username}")
    private String botUsername;

    @Value("${groq.bot.display-name}")
    private String botDisplayName;

    @Override
    public void run(String... args) {
        createGroqBotIfNotExists();
    }

    private void createGroqBotIfNotExists() {
        if (userRepository.existsByUsername(botUsername)) {
            log.info("✅ Bot @{} đã tồn tại, bỏ qua khởi tạo", botUsername);
            return;
        }

        User groqBot = User.builder()
                .username(botUsername)
                .email(botUsername + "@ai.bot")
                .passwordHash(passwordEncoder.encode(
                        "groq_bot_!@#$_not_for_login_" + System.currentTimeMillis()))
                .displayName(botDisplayName)
                .avatarUrl("https://ui-avatars.com/api/?name=Groq+AI"
                         + "&background=7c3aed&color=fff&bold=true")
                .bio("Xin chào! Tôi là Groq AI 🤖 "
                   + "Hãy @groq trong comment để hỏi tôi bất cứ điều gì!")
                .isBot(true)
                .isActive(true)
                .build();

        userRepository.save(groqBot);
        log.info("✅ Đã tạo bot @{} thành công!", botUsername);
    }
}