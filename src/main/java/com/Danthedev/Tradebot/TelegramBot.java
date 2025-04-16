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
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
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
                    sendMessage(chatId, "❌ Something went wrong while executing that command. Please try again.",true);
                    log.warn("Command '{}' from user '{}' failed: {}", messageText, username, e.getMessage());
                }
                return;
            }

            UserState currentState = userState.get(chatId);
            if (currentState == null) {
                sendMessage(chatId, "🤖 I'm not sure what you're trying to do. Use /start to see available commands.",true);
                log.info("User '{}' sent unrecognized message: '{}'", username, messageText);
                return;
            }

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
                        default -> {
                            handleCommand(messageText, chatId, username);
                        log.warn("Unhandled state: {}", currentState);
                        }
                    }
                } catch (Exception e) {
                sendMessage(chatId, "⚠️ Oops! Something went wrong while processing your input.",true);
                log.error("Error processing input in state '{}': {}", currentState, e.getMessage(), e);
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
            default -> sendMessage(chatId, DEFAULT_MESSAGE,false);
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
        sendMessage(chatId, message,false);
    }

    private void handleStatusCommand(long chatId) {
        try {
            String result = marketStatusService.getMarketStatus();
            sendMessage(chatId, result,false);
        } catch (Exception e) {
            sendMessage(chatId, "❌ Unable to retrieve the market status right now. Please try again later.",true);
            log.warn("❗ Error retrieving market status for chatId {}: {}", chatId, e.getMessage(), e);
        }
    }

    private void handleGetFullCryptoResponseCommand(long chatId) {
        userState.put(chatId, UserState.WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE);
        sendMessage(chatId, "Please provide a symbol. Example TON-USDT",false);
    }

    private void handleGetSimpleCryptoResponseCommand(long chatId) {
        userState.put(chatId, UserState.WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE);
        sendMessage(chatId, "Please provide a symbol. Example TON-USDT",false);
    }

    private void handleSearchBySymbolOrByNameCryptoCommand(long chatId) {
        userState.put(chatId,UserState.WAITING_FOR_CRYPTO_SEARCH_SYMBOL);
        sendMessage(chatId, "Please provide a symbol or a name for crypto",false);
    }

    private void handleGetStockResponseCommand(long chatId) {
        userState.put(chatId, UserState.WAITING_FOR_STOCK_SYMBOL);
        sendMessage(chatId, "Please provide a symbol. Example TSLA",false);
    }

    private void handleFindBySymbolCommand(long chatId) {
        userState.put(chatId, UserState.WAITING_FOR_STOCK_SEARCH_SYMBOL);
        sendMessage(chatId, "Please provide a symbol or a name for stock",false);
    }

    private void handleCreateAlertCommand(long chatId) {
        userState.put(chatId, UserState.WAITING_FOR_CREATE_ALERT_SYMBOL);
        sendMessage(chatId, "Please provide a symbol for cryptocurrency. Example TON-USDT",false);
    }

    private void handleShowAlertsCommand(long chatId) {
        List<CryptoAlert> foundAlerts = cryptoService.showAllMyAlerts(chatId);
        sendMessage(chatId, formatCryptoAlertList(foundAlerts),false);
    }

    private void handleDeleteAlertCommand(long chatId) {
        userState.put(chatId, UserState.WAITING_FOR_DELETE_ALERT_SYMBOL);
        sendMessage(chatId, "Please provide a symbol for cryptocurrency. Example TON-USDT",false);
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

        sendMessage(chatId, message,false);
    }

    private void handleGetSupportedDigitalCurrency(long chatId) {
        InputFile file = new InputFile(new File("src/main/resources/digital_currency_list.txt"));
        sendDocument(chatId,file);
    }

    private void handleGetSupportedPhysicalCurrency(long chatId) {
        InputFile file = new InputFile(new File("src/main/resources/physical_currency_list.txt"));
        sendDocument(chatId,file);
    }

    private void processCryptoSymbol(long chatId, String symbol) {
        try {
            if (this.userState.get(chatId).equals(UserState.WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE)) {
                CryptoData result = cryptoService.retrieveCryptoFullInfo(symbol);
                sendMessage(chatId, result.toString(),false);
            } else if (this.userState.get(chatId).equals(UserState.WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE)) {
                JSONObject object = cryptoService.retrieveCryptoPrice(symbol);
                sendMessage(chatId, "Symbol: " + symbol + " \n" +
                        "Price: " + object.getString("price"),false);
            } else if (this.userState.get(chatId).equals(UserState.WAITING_FOR_CREATE_ALERT_SYMBOL)) {
                JSONObject object = cryptoService.retrieveCryptoPrice(symbol);
                String currentPrice = object.getString("price");
                sendMessage(chatId, "Symbol: " + symbol + " \n" +
                        "Current Price: " + currentPrice + "\n" +
                        "Please provide the target price",false);
            } else {
                JSONObject object = cryptoService.searchBySymbolOrByName(symbol);
                JSONObject data = object.getJSONObject("Data");
                if (data == null || !data.has("LIST") || data.getJSONArray("LIST").isEmpty()) {
                    sendMessage(chatId, "⚠️ No matches found for the symbol `" + symbol + "`.",true);
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
                        "Smart Contract Capabilities: " + (smartContractCapabilities ? "Yes" : "No"),false);
            }
        } catch (IOException e) {
            log.warn("400 Bad Request - Malformed symbol '{}'", symbol);
            sendMessage(chatId, "⚠️ Invalid symbol format. Please double-check and try again. Format 'TON-USDT'", true);
        } catch (Exception e) {
            sendMessage(chatId, "❌ Something went wrong. Please double-check and try again. Format 'TON-USDT'", true);
        } finally {
            this.userState.remove(chatId);
        }
    }

    private void processStockSymbol(long chatId, String symbol) {
        try {
            if (this.userState.get(chatId).equals(UserState.WAITING_FOR_STOCK_SYMBOL)) {
                StockData result = stockService.retrieveStockInfo(symbol);
                sendMessage(chatId, result.toString(),false);
            } else {
                List<StockMatch> result = stockService.searchStockBySymbol(symbol);
                String formattedResult = formatStockList(result);
                sendMessage(chatId, formattedResult,false);
            }
        } catch (Exception e) {
            String userMessage = String.format(
                    "⚠️ Oops! We couldn't get data for the symbol: `%s`.\n" +
                            "Please check if it's correct and try again.", symbol
            );
            sendMessage(chatId, userMessage, true);

            log.warn("Failed to retrieve stock data for symbol '{}'. Error: {}", symbol, e.getMessage());
        } finally {
            this.userState.remove(chatId);
        }
    }

    private void processTargetPrice(long chatId, String targetPrice) {
        try {
            if (userState.get(chatId) == UserState.WAITING_FOR_CREATE_ALERT_PRICE) {
                double price = Double.parseDouble(targetPrice);
                if (price <= 0) {
                    sendMessage(chatId, "⚠️ Price must be greater than zero. Please try again.",true);
                    return;
                }

                CryptoAlert alert = alertsList.get(chatId);
                alert.setTargetPrice(price);
                alert.setTelegramUserId(chatId);
                alert.setNotified(false);

                cryptoService.createCryptoAlert(chatId, alert.getSymbol(), price);
                sendMessage(chatId, "✅ Alert created for *" + alert.getSymbol() + "* at target price: *" + price + "*", true);
            }
        } catch (NumberFormatException e) {
            sendMessage(chatId, "❌ Invalid input. Please enter a valid number (e.g. 23450.75).",true);
            log.warn("Invalid price input from {}: {}", chatId, targetPrice);
        } catch (Exception e) {
            sendMessage(chatId, "❌ Something went wrong while creating the alert. Please try again.",true);
            log.error("Error creating alert for {}: {}", chatId, e.getMessage(), e);
        } finally {
            userState.remove(chatId);
            alertsList.remove(chatId);
        }
    }

    private void processDeleteAlert(long chatId, String symbol) {
        try {
            if (userState.get(chatId) == UserState.WAITING_FOR_DELETE_ALERT_SYMBOL) {
                boolean deleted = cryptoService.deleteMyAlert(chatId, symbol);

                if (deleted) {
                    sendMessage(chatId, "🗑️ Alert for *" + symbol.toUpperCase() + "* has been successfully deleted.", true);
                } else {
                    sendMessage(chatId, "⚠️ No alert found for *" + symbol.toUpperCase() + "*.", true);
                }
            }
        } catch (Exception e) {
            sendMessage(chatId, "❌ Failed to delete alert for *" + symbol.toUpperCase() + "*.\nPlease try again later.", true);
            log.error("Error deleting alert for user {} and symbol {}: {}", chatId, symbol, e.getMessage(), e);
        } finally {
            userState.remove(chatId);
        }
    }

    private void processFromCurrency(long chatId, String fromCurrency) {
        if (currencySessionMap.containsKey(chatId) &&
                UserState.WAITING_FOR_FROM_CURRENCY.equals(userState.get(chatId))) {

            fromCurrency = fromCurrency.trim().toUpperCase(); // Normalize input

            if (fromCurrency.matches("[A-Z]{3}")) {
                try {
                    currencySessionMap.put(chatId, fromCurrency);
                    sendMessage(chatId, "✅ You selected *" + fromCurrency + "* as the source currency.\n" +
                            "Now, please provide the _target_ currency symbol (e.g., `EUR`).", true);
                } catch (Exception e) {
                    sendMessage(chatId, "❌ Error processing your request: " + e.getMessage(), true);
                    log.error("Failed to process 'fromCurrency' for user {}: {}", chatId, e.getMessage(), e);
                }
            } else {
                sendMessage(chatId, "❌ Invalid currency symbol. Please provide a valid 3-letter currency code (e.g., `USD`, `EUR`).\n Repeat again the process.", true);
            }
        }
    }

    private void processToCurrency(long chatId, String toCurrency) {
        if (UserState.WAITING_FOR_TO_CURRENCY.equals(this.userState.get(chatId))) {
            try {
                toCurrency = toCurrency.trim().toUpperCase();
                if (!toCurrency.matches("[A-Z]{3}")) {
                    sendMessage(chatId, "❌ Please provide a valid 3-letter currency code (e.g., `EUR`, `USD`).\n Repeat again the process.", true);
                    return;
                }

                String fromCurrency = currencySessionMap.get(chatId);

                if (fromCurrency != null && !fromCurrency.isBlank()) {
                    ExchangeRateData exchangeData = forexService.retrieveCurrencyExchangeRate(fromCurrency, toCurrency);

                    sendMessage(chatId, exchangeData.toString(), false);
                } else {
                    sendMessage(chatId, "⚠️ Could not find the source currency. Please restart the exchange process.", true);
                    log.warn("From currency is missing for user {}", chatId);
                }
            } catch (Exception e) {
                sendMessage(chatId, "❌ An error occurred while fetching the exchange rate. Please try again later.", true);
                log.error("Error fetching exchange rate for user {}: {}", chatId, e.getMessage(), e);
            } finally {
                currencySessionMap.remove(chatId);
                this.userState.remove(chatId);
            }
        }
    }

    private void sendMessage(long chatId, String text, boolean markdown) {
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

    private void sendDocument(Long chatId, InputFile fileToSend) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setDocument(fileToSend);

        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            log.error("Error sending document to chatId {}: {}", chatId, e.getMessage());
        }
    }

    public void sendMessageAlert(Long chatId, String textToSend) {
        sendMessage(chatId,textToSend,true);
    }

    private enum UserState {
        WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE, WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE, WAITING_FOR_CRYPTO_SEARCH_SYMBOL,
        WAITING_FOR_STOCK_SYMBOL, WAITING_FOR_STOCK_SEARCH_SYMBOL, WAITING_FOR_CREATE_ALERT_SYMBOL, WAITING_FOR_CREATE_ALERT_PRICE,
        WAITING_FOR_DELETE_ALERT_SYMBOL,WAITING_FOR_FROM_CURRENCY, WAITING_FOR_TO_CURRENCY
    }
}


