package com.Danthedev.Tradebot.config;

import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;

@Component
@Data
public class BotConfig {

    private String botName = "my_trading_assist_bot";

    private String token = "8166380318:AAFhPMnW8Bjen-ygsquwtE-1KHlnDojJehg";

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }
}
