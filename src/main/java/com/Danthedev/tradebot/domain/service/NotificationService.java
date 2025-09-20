package com.Danthedev.tradebot.domain.service;

public interface NotificationService {

    void sendAlert(Long chatId, String text);
    void sendError(Long chatId, String text);
}
