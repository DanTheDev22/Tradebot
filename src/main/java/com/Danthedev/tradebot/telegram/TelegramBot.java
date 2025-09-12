package com.Danthedev.tradebot.telegram;

import com.Danthedev.tradebot.config.BotConfig;
import com.Danthedev.tradebot.domain.service.*;
import com.Danthedev.tradebot.domain.service.handler.MessageHandlerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final PaymentHandler paymentHandler;
    private final MessageHandlerImpl messageHandler;


    @Autowired
    public TelegramBot(BotConfig botConfig, @Lazy PaymentHandler paymentHandler, @Lazy MessageHandlerImpl messageHandler) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.paymentHandler = paymentHandler;
        this.messageHandler = messageHandler;

    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                handleMessage(update);
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update);
            } else if (update.hasPreCheckoutQuery()) {
                handlePreCheckoutQuery(update);
            }
        } catch (Exception e) {
            log.error("Error handling update: {}", e.getMessage(), e);
        }
    }

    private void handleMessage(Update update) {
        long chatId = update.getMessage().getChatId();

        if (update.getMessage().hasSuccessfulPayment()) {
            paymentHandler.handleSuccessfulPayment(update.getMessage().getSuccessfulPayment(), chatId);
        } else if (update.getMessage().hasText()) {
            messageHandler.routeUserMessage(update);
        }
    }

    private void handleCallbackQuery(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();

        if ("CHECK_PAYMENT".equals(data)) {
            paymentHandler.checkPayment(chatId);
        }
    }

    private void handlePreCheckoutQuery(Update update) {
        long chatId = update.getPreCheckoutQuery().getFrom().getId();
        paymentHandler.handlePaymentUpdate(update, chatId);
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }
}


