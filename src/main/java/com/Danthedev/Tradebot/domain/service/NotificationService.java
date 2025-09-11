package com.Danthedev.Tradebot.domain.service;

public interface NotificationService {

    void sendAlert(Long chatId, String text);
    void sendError(Long chatId, String text);
    void sendConfirmation(Long chatId, String text);
}
