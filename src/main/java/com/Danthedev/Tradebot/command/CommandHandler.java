package com.Danthedev.Tradebot.command;

public interface CommandHandler {
    void handleCommand(String messageText, long chatId, String username);
}
