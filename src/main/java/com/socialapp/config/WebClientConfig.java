package com.socialapp.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

/**
 * Cấu hình WebClient cho GroqService.
 *
 * Lý do cần file này:
 * - Spring Boot auto-config cung cấp WebClient.Builder mặc định,
 * NHƯNG mặc định KHÔNG có timeout. Một request bị treo (do network,
 * DNS, firewall) sẽ block() vĩnh viễn — đây có thể là 1 phần
 * nguyên nhân khiến @groq "không trả lời".
 *
 * - Cấu hình timeout ở đây đảm bảo request tự fail sau 30s
 * thay vì treo mãi.
 *
 * - Wiretap log request/response (chỉ ở header, không log body
 * vì có thể chứa câu hỏi nhạy cảm) để debug.
 */
@Configuration
@Slf4j
public class WebClientConfig {

 @Bean
 public WebClient.Builder webClientBuilder() {
 HttpClient httpClient = HttpClient.create()
 // Timeout mở TCP connection
 .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15_000)
 // Timeout đọc/ghi dữ liệu (15s)
 .doOnConnected(conn -> conn
 .addHandlerLast(new ReadTimeoutHandler(15, TimeUnit.SECONDS))
 .addHandlerLast(new WriteTimeoutHandler(15, TimeUnit.SECONDS)));

 WebClient.Builder builder = WebClient.builder()
 .clientConnector(new ReactorClientHttpConnector(httpClient))
 // Log mỗi request đi ra + response status
 .filter((request, next) -> {
 log.debug("WebClient → {} {}", request.method(), request.url());
 return next.exchange(request)
 .doOnNext(resp -> log.debug(
 "WebClient ← {} {}/{}",
 request.method(), request.url(), resp.statusCode()))
 .doOnError(err -> log.error(
 "WebClient ✗ {} {}: {}",
 request.method(), request.url(), err.getMessage()));
 });

 log.info("WebClient.Builder đã khởi tạo với timeout 15s + wiretap logging");
 return builder;
 }
}
