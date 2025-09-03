package com.Danthedev.Tradebot;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface BotSender {
    void sendMessage(long chatId, String text, boolean markdown);
    void sendMessage(long chatId, String text, boolean markdown, InlineKeyboardMarkup markup);
    void sendDocument(Long chatId, InputFile fileToSend);
    <T extends BotApiMethod<?>> void execute(T method) throws TelegramApiException;
}
