package com.Danthedev.Tradebot;

import com.Danthedev.Tradebot.config.BotConfig;
import com.Danthedev.Tradebot.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final PaymentHandler paymentHandler;
    private final MessageHandler messageHandler;

    @Autowired
    public TelegramBot(BotConfig botConfig, PaymentHandler paymentHandler, MessageHandler messageHandler) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.paymentHandler = paymentHandler;
        this.messageHandler = messageHandler;
    }

    @Override
    public void onUpdateReceived(Update update) {

        try {
            if (update.hasPreCheckoutQuery()) {
                paymentHandler.handlePaymentUpdate(update);
            } else if (update.hasMessage() && update.getMessage().hasText()) {
                messageHandler.routeUserMessage(update);
            }
        } catch (Exception e) {
            log.error("Error handling update: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }
}


