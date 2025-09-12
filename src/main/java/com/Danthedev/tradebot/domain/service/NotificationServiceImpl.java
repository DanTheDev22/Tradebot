package com.Danthedev.tradebot.domain.service;

import com.Danthedev.tradebot.telegram.BotSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final BotSender bot;

    public NotificationServiceImpl(BotSender bot) {
        this.bot = bot;
    }


    @Override
    public void sendAlert(Long chatId, String text) {
        bot.sendWarning(chatId, text);
    }

    @Override
    public void sendError(Long chatId, String text) {
        bot.sendError(chatId, text);
    }

    @Override
    public void sendConfirmation(Long chatId, String text) {
        bot.sendSuccess(chatId, text);
    }
}
