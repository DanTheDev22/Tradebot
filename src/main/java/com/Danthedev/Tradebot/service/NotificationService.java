package com.Danthedev.Tradebot.service;

import com.Danthedev.Tradebot.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private TelegramBot telegramBot;

    public void notifyUser(long chatId, String textMessage) {
        telegramBot.sendMessageAlert(chatId,textMessage);
    }
}
