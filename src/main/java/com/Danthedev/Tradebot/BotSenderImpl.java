package com.Danthedev.Tradebot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotSenderImpl implements BotSender {

    private final TelegramBot bot;

    public void sendMessage(long chatId, String text, boolean markdown) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        if (markdown) message.setParseMode("Markdown");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }

    public void sendMessage(long chatId, String text, boolean markdown, InlineKeyboardMarkup markup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        if (markdown) message.setParseMode("Markdown");
        if (markup != null) message.setReplyMarkup(markup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }

    public void sendDocument(Long chatId, InputFile fileToSend) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setDocument(fileToSend);

        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            log.error("Error sending document to chatId {}: {}", chatId, e.getMessage());
        }
    }

    @Override
    public <T extends BotApiMethod<?>> void execute(T method) throws TelegramApiException {
        bot.execute(method);
    }


    public void sendMessageAlert(Long chatId, String textToSend) {
        sendMessage(chatId,textToSend,true);
    }

}
