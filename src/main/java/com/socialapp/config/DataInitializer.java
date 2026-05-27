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
    private String groqBotUsername;

    @Value("${groq.bot.display-name}")
    private String groqBotDisplayName;

    @Override
    public void run(String... args) {
        // Tạo tài khoản bot Groq AI nếu chưa tồn tại
        if (!userRepository.existsByUsername(groqBotUsername)) {
            User groqBot = User.builder()
                    .username(groqBotUsername)
                    .email("groq@ai.bot")
                    .passwordHash(passwordEncoder.encode("groq_bot_secure_pwd_!@#"))
                    .displayName(groqBotDisplayName)
                    .avatarUrl("https://ui-avatars.com/api/?name=Groq+AI&background=7c3aed&color=fff")
                    .bio("Tôi là Groq AI, hãy @groq để hỏi tôi bất cứ điều gì!")
                    .isBot(true)
                    .isActive(true)
                    .build();

            userRepository.save(groqBot);
            log.info("✅ Groq AI bot account created: @{}", groqBotUsername);
        } else {
            log.info("✅ Groq AI bot already exists: @{}", groqBotUsername);
        }
    }
}