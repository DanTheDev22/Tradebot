package com.Danthedev.Tradebot;

import com.Danthedev.Tradebot.config.BotConfig;
import com.Danthedev.Tradebot.model.User;
import com.Danthedev.Tradebot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final String START_COMMAND = "/start";
    private static final String DEFAULT_MESSAGE = "Something is wrong";

    private final BotConfig botConfig;
    private final UserRepository repository;

    @Autowired
    public TelegramBot(BotConfig botConfig, UserRepository repository) {
        super(botConfig.getToken());
        this.botConfig=botConfig;
        this.repository=repository;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String username = update.getMessage().getFrom().getFirstName();
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();

            handlingNewUsers(chatId,username);
            handlingCommands(messageText,chatId,username);

        }
    }

    private void handlingNewUsers(long chatId, String username) {
        if (chatId != 0 && !findIfExists(username)) {
            User newUser = new User();
            newUser.setUserName(username);
            newUser.setRegistered_at(LocalDateTime.now());
            repository.save(newUser);
        }
    }

    private void handlingCommands(String messageText, long chatId, String username) {
        switch (messageText) {
            case START_COMMAND -> startCommandReceived(chatId,username);
            default -> sendMessage(chatId,DEFAULT_MESSAGE);
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    private boolean findIfExists(String username) {
        return repository.findByUserName(username) != null;

    }

    private void startCommandReceived(Long chatId, String username) {
        String message = "Hi " + username + "! I am TradeBot and I will be your financial assistant!";
        sendMessage(chatId,message);
    }

    private void sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(textToSend);
        sendMessage.setChatId(chatId);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

}
