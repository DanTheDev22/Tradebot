package com.Danthedev.Tradebot;

import com.Danthedev.Tradebot.config.BotConfig;
import com.Danthedev.Tradebot.model.CryptoInfo;
import com.Danthedev.Tradebot.model.User;
import com.Danthedev.Tradebot.repository.UserRepository;
import com.Danthedev.Tradebot.service.CryptoService;
import com.Danthedev.Tradebot.service.MarketStatusService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
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
    private static final String GET_FULL_CRYPTO = "/getfullcrypto";
    private static final String GET_SIMPLE_CRYPTO = "/getsimplecrypto";
    private static final String DEFAULT_MESSAGE = "Sorry, I didn't understand that. Try one of the available commands.";

    private final BotConfig botConfig;
    private final UserRepository repository;
    private final MarketStatusService marketStatusService;
    private final CryptoService cryptoService;

    private final Map<Long,UserState> userState = new HashMap<>();

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
            handleNewUser(chatId,username);

            if (userState.containsKey(chatId) && userState.get(chatId).equals(UserState.WAITING_FOR_SYMBOL_FULL_RESPONSE)) {
                try {
                    processCryptoSymbol(chatId, messageText,UserState.WAITING_FOR_SYMBOL_FULL_RESPONSE);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                userState.remove(chatId);
            } else if (userState.containsKey(chatId) && userState.get(chatId).equals(UserState.WAITING_FOR_SYMBOL_SIMPLE_RESPONSE)) {
                try {
                    processCryptoSymbol(chatId,messageText,UserState.WAITING_FOR_SYMBOL_SIMPLE_RESPONSE);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }else {
                try {
                    handleCommand(messageText,chatId,username);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }


        }
    }

    private void handleNewUser(long chatId, String username) {
        if (chatId != 0 && !findIfExists(username)) {
            User newUser = new User();
            newUser.setUserName(username);
            newUser.setRegistered_at(LocalDateTime.now());
            repository.save(newUser);
        }
    }

    private void handleCommand(String messageText, long chatId, String username) throws IOException, InterruptedException {
        switch (messageText) {
            case START_COMMAND -> startCommandReceived(chatId,username);
            case STATUS_MARKET -> handleStatusCommand(chatId);
            case GET_FULL_CRYPTO-> handleGetFullCryptoResponseCommand(chatId);
            case GET_SIMPLE_CRYPTO -> handleGetSimpleCryptoResponseCommand(chatId);
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

    private void handleStatusCommand(long chatId) {
        try {
            String result = marketStatusService.getMarketStatus();
            sendMessage(chatId, result);
        } catch (Exception e) {
            sendMessage(chatId,"Error retrieving market status");
            log.warn("Error retrieving market status");
        }
    }

    private void handleGetFullCryptoResponseCommand(long chatId) {
        userState.put(chatId,UserState.WAITING_FOR_SYMBOL_FULL_RESPONSE);
        sendMessage(chatId,"Please provide a symbol. Example TON-USDT");
    }
    private void handleGetSimpleCryptoResponseCommand(long chatId) {
        userState.put(chatId,UserState.WAITING_FOR_SYMBOL_SIMPLE_RESPONSE);
        sendMessage(chatId,"Please provide a symbol. Example TON-USDT");
    }

    private void processCryptoSymbol(long chatId, String symbol, UserState userState) throws IOException, InterruptedException {
        if (userState.equals(UserState.WAITING_FOR_SYMBOL_FULL_RESPONSE)) {
            try {
                CryptoInfo result = cryptoService.retrieveCryptoFullInfo(symbol);
                sendMessage(chatId, result.toString());
            } catch (Exception e) {
                sendMessage(chatId, "Error retrieving Crypto Data");
            }
        } else {
            JSONObject object = cryptoService.retrieveCryptoPrice(symbol);
            sendMessage(chatId,"Symbol: " + symbol + " \n" +
                    "Price: " + object.getString("price"));
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

    private enum UserState {
        WAITING_FOR_SYMBOL_FULL_RESPONSE, WAITING_FOR_SYMBOL_SIMPLE_RESPONSE
    }
}


