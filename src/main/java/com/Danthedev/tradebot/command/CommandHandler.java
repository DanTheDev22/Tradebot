package com.Danthedev.tradebot.command;

public interface CommandHandler {
    void handleCommand(String messageText, long chatId, String username);
}
