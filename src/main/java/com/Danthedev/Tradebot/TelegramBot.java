package com.Danthedev.Tradebot;

import com.Danthedev.Tradebot.config.BotConfig;
import com.Danthedev.Tradebot.model.CryptoInfo;
import com.Danthedev.Tradebot.model.ListStock;
import com.Danthedev.Tradebot.model.StockInfo;
import com.Danthedev.Tradebot.model.User;
import com.Danthedev.Tradebot.repository.UserRepository;
import com.Danthedev.Tradebot.service.CryptoService;
import com.Danthedev.Tradebot.service.MarketStatusService;
import com.Danthedev.Tradebot.service.StockService;
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
import java.util.List;
import java.util.Map;

import static com.Danthedev.Tradebot.model.ListStock.formattedList;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final String START_COMMAND = "/start";
    private static final String STATUS_MARKET = "/status";
    private static final String GET_FULL_CRYPTO = "/getfullcrypto";
    private static final String GET_SIMPLE_CRYPTO = "/getsimplecrypto";
    private static final String GET_STOCK = "/getstock";
    private static final String FIND_STOCK ="/findstock";
    private static final String DEFAULT_MESSAGE = "Sorry, I didn't understand that. Try one of the available commands.";

    private final BotConfig botConfig;
    private final UserRepository repository;
    private final MarketStatusService marketStatusService;
    private final CryptoService cryptoService;
    private final StockService stockService;

    private final Map<Long,UserState> userState = new HashMap<>();


    @Autowired
    public TelegramBot(BotConfig botConfig, UserRepository repository,MarketStatusService marketStatusService, CryptoService cryptoService,StockService stockService) {
        super(botConfig.getToken());
        this.botConfig=botConfig;
        this.repository=repository;
        this.marketStatusService=marketStatusService;
        this.cryptoService=cryptoService;
        this.stockService=stockService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String username = update.getMessage().getFrom().getFirstName();
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            handleNewUser(chatId, username);
            if (messageText.startsWith("/")) {
                try {
                    handleCommand(messageText,chatId,username);
                } catch (Exception e) {
                    sendMessage(chatId, "❌ Command failed: " + e.getMessage());
                    log.warn("Command failed: {}", e.getMessage());
                }
                return;
            }

            UserState currentState = userState.get(chatId);
            if (currentState != null) {
                try {
                    switch (userState.get(chatId)) {
                        case WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE ->
                                processCryptoSymbol(chatId, messageText, UserState.WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE);
                        case WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE ->
                                processCryptoSymbol(chatId, messageText, UserState.WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE);
                        case WAITING_FOR_STOCK_SYMBOL ->
                                processStockSymbol(chatId, messageText, UserState.WAITING_FOR_STOCK_SYMBOL);
                        case WAITING_FOR_SYMBOL ->
                                processStockSymbol(chatId, messageText, UserState.WAITING_FOR_SYMBOL);
                        default -> handleCommand(messageText, chatId, username);
                    }
                } catch (Exception e) {
                    sendMessage(chatId, "⚠️ Error processing your input: " + e.getMessage());
                    log.error("State input failed", e);
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
            case GET_STOCK -> handleGetStockResponseCommand(chatId);
            case FIND_STOCK -> handleFindBySymbolCommand(chatId);
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
        String message = "Hi " + username + "! I am TradeBot and I will be your financial assistant! \n" +
                "\n Use the following commands: \n" +
                "/status - shows the US market status \n" +
                "/getsimplecrypto - shows the price of a cryptocurrency \n" +
                "/getfullcrypto - shows  full details about a cryptocurrency \n" +
                "/getstock - shows  full details about an stock \n" +
                "/findstock - returns the best-matching symbols and market information";
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
        userState.put(chatId,UserState.WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE);
        sendMessage(chatId,"Please provide a symbol. Example TON-USDT");
    }

    private void handleGetSimpleCryptoResponseCommand(long chatId) {
        userState.put(chatId,UserState.WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE);
        sendMessage(chatId,"Please provide a symbol. Example TON-USDT");
    }

    private void handleGetStockResponseCommand(long chatId) {
        userState.put(chatId,UserState.WAITING_FOR_STOCK_SYMBOL);
        sendMessage(chatId,"Please provide a symbol. Example TSLA");
    }

    private void handleFindBySymbolCommand(long chatId) {
        userState.put(chatId,UserState.WAITING_FOR_SYMBOL);
        sendMessage(chatId,"Please provide a symbol or a name for stock");
    }

    private void processCryptoSymbol(long chatId, String symbol, UserState userState) throws IOException, InterruptedException {
        try {
            if (userState.equals(UserState.WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE)) {
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
        } catch (Exception e) {
            sendMessage(chatId, "⚠️ Could not retrieve data for symbol: `" + symbol + "`\nError: " + e.getMessage());
            log.warn("Error retrieving Crypto Data");
        } finally {
            this.userState.remove(chatId);
        }
    }

    private void processStockSymbol(long chatId, String symbol, UserState userState) throws IOException, InterruptedException {
        try {
            if (userState.equals(UserState.WAITING_FOR_STOCK_SYMBOL)) {
                StockInfo result = stockService.retrieveStockInfo(symbol);
                sendMessage(chatId, result.toString());
            } else {
                List<ListStock> result = stockService.searchStockBySymbol(symbol);
                String formattedResult = formattedList(result);
                sendMessage(chatId, formattedResult);
            }
        } catch (Exception e) {
            sendMessage(chatId, "⚠️ Could not retrieve data for symbol: `" + symbol + "`\nError: " + e.getMessage());
            log.warn("Error retrieving Stock Data");
        } finally {
            this.userState.remove(chatId);
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
        WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE, WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE,WAITING_FOR_STOCK_SYMBOL,WAITING_FOR_SYMBOL
    }
}


