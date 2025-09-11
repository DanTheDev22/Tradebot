package com.Danthedev.Tradebot.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotSenderImpl implements BotSender {

    private TelegramBot bot;

    @Autowired
    public void setBot(@Lazy TelegramBot bot) {
        this.bot = bot;
    }


    @Override
    public void sendText(long chatId, String text, boolean markdown) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        if (markdown) message.setParseMode("Markdown");
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }

    @Override
    public void sendText(long chatId, String text, boolean markdown, InlineKeyboardMarkup markup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        if (markdown) message.setParseMode("Markdown");
        if (markup != null) message.setReplyMarkup(markup);
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }

    @Override
    public void sendError(long chatId, String message) {
        sendText(chatId, "❌ " + message, true);
    }

    @Override
    public void sendSuccess(long chatId, String message) {
        sendText(chatId, "✅ " + message, true);
    }

    @Override
    public void sendWarning(long chatId, String message) {
        sendText(chatId, "⚠️ " + message, true);
    }

    public void sendDocument(Long chatId, InputFile fileToSend) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setDocument(fileToSend);

        try {
            bot.execute(sendDocument);
        } catch (TelegramApiException e) {
            log.error("Error sending document to chatId {}: {}", chatId, e.getMessage());
        }
    }

    @Override
    public void sendInvoice(org.telegram.telegrambots.meta.api.methods.send.SendInvoice invoice) {
        try {
            bot.execute(invoice);
        } catch (TelegramApiException e) {
            log.error("Failed to send invoice", e);
        }
    }


    @Override
    public void answerPreCheckoutQuery(AnswerPreCheckoutQuery answer) {
        try {
            bot.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Failed to answer PreCheckoutQuery", e);
        }
    }

}
