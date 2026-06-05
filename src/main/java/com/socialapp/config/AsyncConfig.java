package com.socialapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Bật @Async để AiMentionService.handleIfMentioned()
 * chạy trên thread riêng, không block response của user.
 */
@Configuration
@EnableAsync
public class AsyncConfig {}