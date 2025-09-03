package com.Danthedev.Tradebot.service;

public interface CommandHandler {
    void handleCommand(String messageText, long chatId, String username);
}
