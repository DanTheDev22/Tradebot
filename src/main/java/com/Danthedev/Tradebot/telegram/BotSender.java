package com.Danthedev.Tradebot.telegram;

import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface BotSender {
    void sendText(long chatId, String text, boolean markdown);
    void sendText(long chatId, String text, boolean markdown, InlineKeyboardMarkup markup);

    void sendError(long chatId, String message);
    void sendSuccess(long chatId, String message);
    void sendWarning(long chatId, String message);

    void sendDocument(Long chatId, InputFile fileToSend);
    void sendInvoice(SendInvoice invoice);
    void answerPreCheckoutQuery(AnswerPreCheckoutQuery answer);
}
