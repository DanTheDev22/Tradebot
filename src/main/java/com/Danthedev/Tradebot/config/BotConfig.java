package com.Danthedev.Tradebot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Data
public class BotConfig {

    private String botName = "my_trading_assist_bot";

    private String token = "8166380318:AAFhPMnW8Bjen-ygsquwtE-1KHlnDojJehg";
}
