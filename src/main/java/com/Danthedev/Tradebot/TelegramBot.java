package com.Danthedev.Tradebot;

import com.Danthedev.Tradebot.config.BotConfig;
import com.Danthedev.Tradebot.model.CryptoInfo;
import com.Danthedev.Tradebot.model.User;
import com.Danthedev.Tradebot.repository.UserRepository;
import com.Danthedev.Tradebot.service.CryptoService;
import com.Danthedev.Tradebot.service.MarketStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final String START_COMMAND = "/start";
    private static final String STATUS_MARKET = "/status";
    private static final String GET_CRYPTO = "/getcrypto";
    private static final String DEFAULT_MESSAGE = "Something is wrong";

    private final BotConfig botConfig;
    private final UserRepository repository;
    private final MarketStatusService marketStatusService;
    private final CryptoService cryptoService;

    private Map<Long,String> userState = new HashMap<>();

    @Autowired
    public TelegramBot(BotConfig botConfig, UserRepository repository,MarketStatusService marketStatusService, CryptoService cryptoService) {
        super(botConfig.getToken());
        this.botConfig=botConfig;
        this.repository=repository;
        this.marketStatusService=marketStatusService;
        this.cryptoService=cryptoService;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String username = update.getMessage().getFrom().getFirstName();
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            handlingNewUsers(chatId,username);

            if (userState.containsKey(chatId) && userState.get(chatId).equals("WAITING_FOR_SYMBOL")) {
                try {
                    processCryptoSymbol(chatId,messageText);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                userState.remove(chatId);
            } else {
                try {
                    handlingCommands(messageText,chatId,username);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }


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

    private void handlingCommands(String messageText, long chatId, String username) throws IOException, InterruptedException {
        switch (messageText) {
            case START_COMMAND -> startCommandReceived(chatId,username);
            case STATUS_MARKET -> statusMarketCommandReceived(chatId);
            case GET_CRYPTO -> handleGetCryptoCommand(chatId);
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

    private void statusMarketCommandReceived(long chatId) throws IOException {
        try {
            String result = marketStatusService.getMarketStatus();
            sendMessage(chatId, result);
        } catch (Exception e) {
            sendMessage(chatId,"Error retrieving market status");
            log.warn("Error retrieving market status");
        }
    }

    private void handleGetCryptoCommand(long chatId) {
        userState.put(chatId,"WAITING_FOR_SYMBOL");
        sendMessage(chatId,"Please provide a symbol. Example TON-USDT");
    }

    private void processCryptoSymbol(long chatId, String symbol) throws IOException, InterruptedException {
        try {
            CryptoInfo result = cryptoService.retrieveCryptoInfo(symbol);
            sendMessage(chatId, result.toString());
        } catch (Exception e) {
            sendMessage(chatId,"Error retrieving Crypto Data");
        }
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
