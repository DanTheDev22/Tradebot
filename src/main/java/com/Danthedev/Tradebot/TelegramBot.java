package com.Danthedev.Tradebot;

import com.Danthedev.Tradebot.config.BotConfig;
import com.Danthedev.Tradebot.dto.CryptoData;
import com.Danthedev.Tradebot.dto.ExchangeRateData;
import com.Danthedev.Tradebot.dto.StockData;
import com.Danthedev.Tradebot.dto.StockMatch;
import com.Danthedev.Tradebot.model.*;
import com.Danthedev.Tradebot.repository.UserRepository;
import com.Danthedev.Tradebot.service.CryptoService;
import com.Danthedev.Tradebot.service.ForexService;
import com.Danthedev.Tradebot.service.MarketStatusService;
import com.Danthedev.Tradebot.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.Danthedev.Tradebot.TradebotCommands.*;
import static com.Danthedev.Tradebot.TradebotCommands.AlertCommands.*;
import static com.Danthedev.Tradebot.TradebotCommands.CryptoCommands.*;
import static com.Danthedev.Tradebot.TradebotCommands.MarketCommands.*;
import static com.Danthedev.Tradebot.TradebotCommands.StockCommands.*;
import static com.Danthedev.Tradebot.dto.StockMatch.formatStockList;
import static com.Danthedev.Tradebot.model.CryptoAlert.formatCryptoAlertList;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final UserRepository userRepository;
    private final MarketStatusService marketStatusService;
    private final CryptoService cryptoService;
    private final StockService stockService;
    private final ForexService forexService;

    private final Map<Long, UserState> userState = new HashMap<>();
    private final Map<Long, CryptoAlert> alertsList = new HashMap<>();
    private final Map<Long, String> currencySessionMap = new HashMap<>();

    @Autowired
    public TelegramBot(BotConfig botConfig, UserRepository userRepository, MarketStatusService marketStatusService,
                       CryptoService cryptoService, StockService stockService, ForexService forexService) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.userRepository = userRepository;
        this.marketStatusService = marketStatusService;
        this.cryptoService = cryptoService;
        this.stockService = stockService;
        this.forexService=forexService;
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
                    handleCommand(messageText, chatId, username);
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
                        case WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE, WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE,
                             WAITING_FOR_CRYPTO_SEARCH_SYMBOL ->
                                processCryptoSymbol(chatId, messageText);
                        case WAITING_FOR_STOCK_SYMBOL, WAITING_FOR_STOCK_SEARCH_SYMBOL ->
                                processStockSymbol(chatId, messageText);
                        case WAITING_FOR_FROM_CURRENCY -> {
                            processFromCurrency(chatId, messageText);
                            userState.put(chatId,UserState.WAITING_FOR_TO_CURRENCY);
                        }
                        case WAITING_FOR_TO_CURRENCY ->
                                processToCurrency(chatId,messageText);
                        case WAITING_FOR_CREATE_ALERT_SYMBOL -> {
                            processCryptoSymbol(chatId, messageText);
                            CryptoAlert newAlert = new CryptoAlert();
                            newAlert.setSymbol(messageText.toUpperCase());
                            alertsList.put(chatId, newAlert);
                            userState.put(chatId, UserState.WAITING_FOR_CREATE_ALERT_PRICE);
                        }
                        case WAITING_FOR_CREATE_ALERT_PRICE ->
                                processTargetPrice(chatId, messageText);
                        case WAITING_FOR_DELETE_ALERT_SYMBOL ->
                                processDeleteAlert(chatId,messageText);
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
        if (chatId == 0) return;
        userRepository.insertIfNotExists(chatId,username,LocalDateTime.now());
    }

    private void handleCommand(String messageText, long chatId, String username) {
        switch (messageText) {
            case START_COMMAND -> startCommandReceived(chatId, username);
            case STATUS_MARKET -> handleStatusCommand(chatId);
            case GET_SUPPORTED_DIGITAL_CURRENCY -> handleGetSupportedDigitalCurrency(chatId);
            case GET_SUPPORTED_PHYSICAL_CURRENCY -> handleGetSupportedPhysicalCurrency(chatId);
            case GET_FULL_CRYPTO -> handleGetFullCryptoResponseCommand(chatId);
            case GET_SIMPLE_CRYPTO -> handleGetSimpleCryptoResponseCommand(chatId);
            case FIND_CRYPTO -> handleSearchBySymbolOrByNameCryptoCommand(chatId);
            case GET_STOCK -> handleGetStockResponseCommand(chatId);
            case FIND_STOCK -> handleFindBySymbolCommand(chatId);
            case EXCHANGE_RATE -> handleExchangeCommand(chatId);
            case CREATE_ALERT -> handleCreateAlertCommand(chatId);
            case SHOW_ALERTS -> handleShowAlertsCommand(chatId);
            case DELETE_ALERT -> handleDeleteAlertCommand(chatId);
            default -> sendMessage(chatId, DEFAULT_MESSAGE);
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    private void startCommandReceived(Long chatId, String username) {
        String message = "Hi " + username + "! I am TradeBot and I will be your financial assistant! \n" +
                "\n Use the following commands: \n" +
                "/status - shows the US market status \n" +
                "/suppcrypto - returns a list with all CryptoCurrencies supported for Exchange \n" +
                "/suppcurrency - returns a list with all Physical Currencies supported for Exchange \n" +
                "/getsimplecrypto - shows the price of a cryptocurrency \n" +
                "/getfullcrypto - shows  full details about a cryptocurrency \n" +
                "/findcrypto - returns the best-matching symbols and market information for cryptocurrency \n" +
                "/getstock - shows  full details about an stock \n" +
                "/findstock - returns the best-matching symbols and market information for stock \n" +
                "/exchange - returns the realtime exchange rate for a pair of digital currency (e.g., Bitcoin) or physical currency (e.g., USD). \n" +
                "/createalert - creates alert price and gives notification when is reached \n" +
                "/showalerts - shows user's alerts \n" +
                "/deletealert - delete user's alert \n";
        sendMessage(chatId, message);
    }

    private void handleStatusCommand(long chatId) {
        try {
            String result = marketStatusService.getMarketStatus();
            sendMessage(chatId, result);
        } catch (Exception e) {
            sendMessage(chatId, "Error retrieving market status");
            log.warn("Error retrieving market status");
        }
    }

    private void handleGetFullCryptoResponseCommand(long chatId) {
        userState.put(chatId, UserState.WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE);
        sendMessage(chatId, "Please provide a symbol. Example TON-USDT");
    }

    private void handleGetSimpleCryptoResponseCommand(long chatId) {
        userState.put(chatId, UserState.WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE);
        sendMessage(chatId, "Please provide a symbol. Example TON-USDT");
    }

    private void handleSearchBySymbolOrByNameCryptoCommand(long chatId) {
        userState.put(chatId,UserState.WAITING_FOR_CRYPTO_SEARCH_SYMBOL);
        sendMessage(chatId, "Please provide a symbol or a name for crypto");
    }

    private void handleGetStockResponseCommand(long chatId) {
        userState.put(chatId, UserState.WAITING_FOR_STOCK_SYMBOL);
        sendMessage(chatId, "Please provide a symbol. Example TSLA");
    }

    private void handleFindBySymbolCommand(long chatId) {
        userState.put(chatId, UserState.WAITING_FOR_STOCK_SEARCH_SYMBOL);
        sendMessage(chatId, "Please provide a symbol or a name for stock");
    }

    private void handleCreateAlertCommand(long chatId) {
        userState.put(chatId, UserState.WAITING_FOR_CREATE_ALERT_SYMBOL);
        sendMessage(chatId, "Please provide a symbol for cryptocurrency. Example TON-USDT");
    }

    private void handleShowAlertsCommand(long chatId) {
        List<CryptoAlert> foundAlerts = cryptoService.showAllMyAlerts(chatId);
        sendMessage(chatId, formatCryptoAlertList(foundAlerts));
    }

    private void handleDeleteAlertCommand(long chatId) {
        userState.put(chatId, UserState.WAITING_FOR_DELETE_ALERT_SYMBOL);
        sendMessage(chatId, "Please provide a symbol for cryptocurrency. Example TON-USDT");
    }

    private void handleExchangeCommand(long chatId) {
        userState.put(chatId, UserState.WAITING_FOR_FROM_CURRENCY);
        currencySessionMap.put(chatId, null);

        String message = """
                Please provide the symbol for the currency/digital currency \
                you want to exchange from (e.g., USD, EUR, BTC, ETH).
                Check supported Digital Currencies - /suppcrypto\s
                Check supported Physical Currencies - /suppcurrency\s
                """;

        sendMessage(chatId, message);
    }

    private void handleGetSupportedDigitalCurrency(long chatId) {
    }

    private void handleGetSupportedPhysicalCurrency(long chatId) {
    }

    private void processCryptoSymbol(long chatId, String symbol) {
        try {
            if (this.userState.get(chatId).equals(UserState.WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE)) {
                CryptoData result = cryptoService.retrieveCryptoFullInfo(symbol);
                sendMessage(chatId, result.toString());
            } else if (this.userState.get(chatId).equals(UserState.WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE)) {
                JSONObject object = cryptoService.retrieveCryptoPrice(symbol);
                sendMessage(chatId, "Symbol: " + symbol + " \n" +
                        "Price: " + object.getString("price"));
            } else if (this.userState.get(chatId).equals(UserState.WAITING_FOR_CREATE_ALERT_SYMBOL)) {
                JSONObject object = cryptoService.retrieveCryptoPrice(symbol);
                String currentPrice = object.getString("price");
                sendMessage(chatId, "Symbol: " + symbol + " \n" +
                        "Current Price: " + currentPrice + "\n" +
                        "Please provide the target price");
            } else {
                JSONObject object = cryptoService.searchBySymbolOrByName(symbol);
                JSONObject data = object.getJSONObject("Data");
                if (data == null || !data.has("LIST") || data.getJSONArray("LIST").isEmpty()) {
                    sendMessage(chatId, "⚠️ No matches found for the symbol `" + symbol + "`.");
                    return;
                }

                JSONArray bestMatchCrypto = data.getJSONArray("LIST");
                JSONObject cryptoMatch = bestMatchCrypto.getJSONObject(0);

                String correctSymbol = cryptoMatch.getString("SYMBOL");
                String correctName = cryptoMatch.getString("NAME");
                String type = cryptoMatch.getString("ASSET_TYPE");
                boolean smartContractCapabilities = cryptoMatch.getBoolean("HAS_SMART_CONTRACT_CAPABILITIES");
                sendMessage(chatId,"Crypto Match Details:\n" +
                        "Symbol: " + correctSymbol + "\n" +
                        "Name: " + correctName + "\n" +
                        "Type: " + type + "\n" +
                        "Smart Contract Capabilities: " + (smartContractCapabilities ? "Yes" : "No"));
            }
        } catch (IOException e) {
            sendMessage(chatId, "⚠️ Error retrieving data for symbol: `" + symbol + "`\nNetwork error: " + e.getMessage());
            log.warn("Network error while retrieving crypto data", e);
        } catch (JSONException e) {
            sendMessage(chatId, "⚠️ Could not parse the response for symbol: `" + symbol + "`\nError: " + e.getMessage());
            log.warn("Error parsing the JSON response", e);
        } catch (Exception e) {
            sendMessage(chatId, "⚠️ Could not retrieve data for symbol: `" + symbol + "`\nError: " + e.getMessage());
            log.warn("Unexpected error while retrieving crypto data", e);
        } finally {
            this.userState.remove(chatId);
        }
    }


    private void processStockSymbol(long chatId, String symbol) {
        try {
            if (this.userState.get(chatId).equals(UserState.WAITING_FOR_STOCK_SYMBOL)) {
                StockData result = stockService.retrieveStockInfo(symbol);
                sendMessage(chatId, result.toString());
            } else {
                List<StockMatch> result = stockService.searchStockBySymbol(symbol);
                String formattedResult = formatStockList(result);
                sendMessage(chatId, formattedResult);
            }
        } catch (Exception e) {
            sendMessage(chatId, "⚠️ Could not retrieve data for symbol: `" + symbol + "`\nError: " + e.getMessage());
            log.warn("Error retrieving Stock Data");
        } finally {
            this.userState.remove(chatId);
        }
    }

    private void processTargetPrice(long chatId, String targetPrice) {
        try {
            if (this.userState.get(chatId).equals(UserState.WAITING_FOR_CREATE_ALERT_PRICE)) {
                CryptoAlert alert = alertsList.get(chatId);
                double price = Double.parseDouble(targetPrice);
                alert.setTargetPrice(price);
                alert.setTelegramUserId(chatId);
                alert.setNotified(false);
                cryptoService.createCryptoAlert(chatId, alert.getSymbol(), alert.getTargetPrice());
                sendMessage(chatId, "✅ Alert created successfully for " + alert.getSymbol() + " at price: " + price);
            }
        } catch (Exception e) {
            sendMessage(chatId, "⚠️ Invalid price. Please enter a valid number.");
        }
        this.userState.remove(chatId);
        alertsList.remove(chatId);
    }

    private void processDeleteAlert(long chatId, String symbol) {
        try {
            if (this.userState.get(chatId).equals(UserState.WAITING_FOR_DELETE_ALERT_SYMBOL)) {
                cryptoService.deleteMyAlert(chatId, symbol);
                sendMessage(chatId, "🗑️ Alert for " + symbol.toUpperCase() + " deleted (if it existed).");
            }
        } catch (Exception e) {
                sendMessage(chatId, "⚠️ Failed to delete alert for " + symbol + ": " + e.getMessage());
                log.error("Error deleting alert", e);
            } finally {
                this.userState.remove(chatId);
            }
    }

    private void processFromCurrency(long chatId, String fromCurrency) {
        try {
            if (currencySessionMap.containsKey(chatId) && this.userState.get(chatId).equals(UserState.WAITING_FOR_FROM_CURRENCY)) {
                currencySessionMap.put(chatId, fromCurrency);
                sendMessage(chatId, "You selected " + fromCurrency + ". Now, please provide the 'to' currency symbol (e.g., EUR).");
            }
        } catch (Exception e) {
            sendMessage(chatId,"Error retrieving data : " + e.getMessage());
        }
    }

    private void processToCurrency(long chatId, String toCurrency) {
        if (this.userState.get(chatId).equals(UserState.WAITING_FOR_TO_CURRENCY)) {
            try {
                String fromCurrency = currencySessionMap.get(chatId);

                if (fromCurrency != null) {
                    ExchangeRateData exchangeData = forexService.retrieveCurrencyExchangeRate(fromCurrency, toCurrency);
                    sendMessage(chatId, exchangeData.toString());
                }
            } catch (Exception e) {
                sendMessage(chatId, "Not working properly : " + e.getMessage());
            } finally {
                currencySessionMap.remove(chatId);
                this.userState.remove(chatId);
            }
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

    private void sendDocument(Long chatId, InputFile fileToSend) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setDocument(fileToSend);
        sendDocument.setChatId(chatId);
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendMessageAlert(Long chatId, String textToSend) {
        sendMessage(chatId,textToSend);
    }

    private enum UserState {
        WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE, WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE, WAITING_FOR_CRYPTO_SEARCH_SYMBOL,
        WAITING_FOR_STOCK_SYMBOL, WAITING_FOR_STOCK_SEARCH_SYMBOL, WAITING_FOR_CREATE_ALERT_SYMBOL, WAITING_FOR_CREATE_ALERT_PRICE,
        WAITING_FOR_DELETE_ALERT_SYMBOL,WAITING_FOR_FROM_CURRENCY, WAITING_FOR_TO_CURRENCY
    }
}


