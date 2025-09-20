package com.Danthedev.tradebot.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotSenderImpl implements BotSender {

    private final ObjectProvider<TelegramBot> botProvider;

    private TelegramLongPollingBot getBot() {
        return botProvider.getIfAvailable();
    }

    @Override
    public void sendText(long chatId, String text, boolean markdown) {
        TelegramBot bot = botProvider.getIfAvailable();

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
        TelegramBot bot = botProvider.getIfAvailable();

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

    public void sendDocument(Long chatId, String resourcePath) {
        TelegramBot bot = botProvider.getIfAvailable();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                log.error("Resource not found: {}", resourcePath);
                sendError(chatId, "❌ Document not found.");
                return;
            }
            InputFile inputFile = new InputFile(is, resourcePath); // Telegram InputFile cu InputStream
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setDocument(inputFile);

            bot.execute(sendDocument);
        } catch (TelegramApiException | IOException e) {
            log.error("Error sending document to chatId {}: {}", chatId, e.getMessage());
        }
    }

    @Override
    public void sendInvoice(org.telegram.telegrambots.meta.api.methods.send.SendInvoice invoice) {
        TelegramBot bot = botProvider.getIfAvailable();

        try {
            bot.execute(invoice);
        } catch (TelegramApiException e) {
            log.error("Failed to send invoice", e);
        }
    }

    @Override
    public void answerPreCheckoutQuery(AnswerPreCheckoutQuery answer) {
        TelegramBot bot = botProvider.getIfAvailable();

        try {
            bot.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Failed to answer PreCheckoutQuery", e);
        }
    }
}
