package com.Danthedev.Tradebot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;


@Configuration
@Data
public class BotConfig {

    @Value("${tradebot.name}")
    private String botName;

    @Value("${tradebot.token}")
    private String token;

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }
}
