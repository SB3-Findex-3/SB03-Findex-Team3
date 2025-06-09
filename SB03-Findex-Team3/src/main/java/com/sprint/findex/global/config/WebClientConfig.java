package com.sprint.findex.global.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
            // 연결 타임아웃 설정 (5초)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            // 응답 타임아웃 설정 (30초)
            .responseTimeout(Duration.ofSeconds(30))
            // 읽기/쓰기 타임아웃 설정 (30초)
            .doOnConnected(conn -> {
                conn.addHandlerLast(new io.netty.handler.timeout.ReadTimeoutHandler(30))
                    .addHandlerLast(new io.netty.handler.timeout.WriteTimeoutHandler(30));
            });

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}