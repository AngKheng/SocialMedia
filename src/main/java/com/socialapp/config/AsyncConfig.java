package com.socialapp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Bật @Async với custom thread pool + error handler.
 *
 * Lý do không dùng mặc định (SimpleAsyncTaskExecutor):
 * - Tạo thread mới cho mỗi lần gọi → có thể leak hàng trăm thread
 * - Không có error handler → exception trên thread async
 * bị nuốt gần như hoàn toàn (chỉ log qua Spring default,
 * thường không hiện trong console VSCode)
 *
 * Thread pool:
 * - corePoolSize=2: đủ xử lý bình thường
 * - maxPoolSize=5: chịu tải khi nhiều user @groq cùng lúc
 * - queueCapacity=50: buffer khi pool đầy
 * - CallerRunsPolicy: nếu queue đầy → caller (request thread)
 * tự chạy task. Giúp back-pressure tự nhiên, tránh mất request.
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

 @Override
 public Executor getAsyncExecutor() {
 ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
 executor.setCorePoolSize(2);
 executor.setMaxPoolSize(5);
 executor.setQueueCapacity(50);
 executor.setThreadNamePrefix("groq-ai-");
 executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
 // Đợi task trong queue xử lý xong khi shutdown
 executor.setWaitForTasksToCompleteOnShutdown(true);
 executor.setAwaitTerminationSeconds(30);
 executor.initialize();
 log.info("Async executor cho @groq đã khởi tạo: core=2, max=5, queue=50");
 return executor;
 }

 /**
 * Error handler cho các @Async method trả về void.
 * (Các method trả về Future/CompletableFuture thì xử lý riêng.)
 *
 * Log đầy đủ class + method + params + stack trace
 * để debug lỗi trên thread async.
 */
 @Override
 public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
 return (throwable, method, params) -> {
 log.error("Async uncaught error in {}.{} with params {}",
 method.getDeclaringClass().getSimpleName(),
 method.getName(),
 params,
 throwable);
 };
 }
}
