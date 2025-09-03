package com.Danthedev.Tradebot.service;

import com.Danthedev.Tradebot.BotSenderImpl;
import com.Danthedev.Tradebot.TelegramBot;
import com.Danthedev.Tradebot.UserState;
import com.Danthedev.Tradebot.dto.CryptoData;
import com.Danthedev.Tradebot.dto.ExchangeRateData;
import com.Danthedev.Tradebot.dto.StockData;
import com.Danthedev.Tradebot.dto.StockMatch;
import com.Danthedev.Tradebot.model.CryptoAlert;
import com.Danthedev.Tradebot.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.Danthedev.Tradebot.TradebotCommands.NO_ACCESS;
import static com.Danthedev.Tradebot.dto.StockMatch.formatStockList;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandler {

    @Value("${RunPay.API.token}")
    private String RunPayToken;

    private final BotSenderImpl bot;
    private final UserStateService userStateService;
    private final PaymentHandler paymentHandler;
    private final CommandHandlerImpl commandHandler;
    private final ForexService forexService;
    private final StockService stockService;
    private final CryptoService cryptoService;
    private final CryptoPayClient cryptoPayClient;
    private final UserRepository userRepository;

    private final Map<Long, CryptoAlert> alertsList = new HashMap<>();
    private final Map<UserState, BiConsumer<Long, String>> stateHandlers = new HashMap<>();

    @PostConstruct
    private void initStateHandlers() {
        stateHandlers.put(UserState.WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE, this::processCryptoSymbol);
        stateHandlers.put(UserState.WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE, this::processCryptoSymbol);
        stateHandlers.put(UserState.WAITING_FOR_CRYPTO_SEARCH_SYMBOL, this::processCryptoSymbol);

        stateHandlers.put(UserState.WAITING_FOR_STOCK_SYMBOL, this::processStockSymbol);
        stateHandlers.put(UserState.WAITING_FOR_STOCK_SEARCH_SYMBOL, this::processStockSymbol);

        stateHandlers.put(UserState.WAITING_FOR_FROM_CURRENCY, (chatId, messageText) -> {
            processFromCurrency(chatId, messageText); // pass the actual message text
            userStateService.setUserState(chatId, UserState.WAITING_FOR_TO_CURRENCY);
        });

        stateHandlers.put(UserState.WAITING_FOR_TO_CURRENCY, this::processToCurrency);

        stateHandlers.put(UserState.WAITING_FOR_CREATE_ALERT_SYMBOL, (chatId, messageText) -> {
            processCryptoSymbol(chatId, messageText);

            CryptoAlert newAlert = new CryptoAlert();
            newAlert.setSymbol(messageText.toUpperCase());
            alertsList.put(chatId, newAlert);
            userStateService.setUserState(chatId, UserState.WAITING_FOR_CREATE_ALERT_PRICE);
        });

        stateHandlers.put(UserState.WAITING_FOR_CREATE_ALERT_PRICE, this::processTargetPrice);
        stateHandlers.put(UserState.WAITING_FOR_DELETE_ALERT_SYMBOL, this::processDeleteAlert);
    }


    public void routeUserMessage(Update update) {
        String username = update.getMessage().getFrom().getFirstName();
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        handleNewUser(chatId, username);

        if (!paymentHandler.checkForAccess(chatId)) {
            bot.sendMessage(chatId,NO_ACCESS,true);
            try {
                CryptoPayClient.Invoice invoiceOne = new CryptoPayClient.Invoice(
                        "crypto",
                        "TON",
                        null,
                        2.0,
                        "Test TON Payment",
                        "Regular subscribtion ",
                        3600
                );
                String invoiceLink1 = cryptoPayClient.createInvoice(invoiceOne);

                CryptoPayClient.Invoice invoiceTwo = new CryptoPayClient.Invoice(
                        "fiat",
                        null,
                        "USD",
                        10.0,
                        "Test USD Payment",
                        "Regular subscribtion",
                        3600
                );
                String invoiceLink2 = cryptoPayClient.createInvoice(invoiceTwo);


                InlineKeyboardButton cryptoPay = new InlineKeyboardButton();
                cryptoPay.setText("CryptoApi Pay");
                cryptoPay.setUrl(invoiceLink1);

                InlineKeyboardButton fiatPay = new InlineKeyboardButton();
                fiatPay.setText("FiatApi Pay");
                fiatPay.setUrl(invoiceLink2);



                InlineKeyboardMarkup cryptoMarkup  = InlineKeyboardMarkup.builder()
                        .keyboard(List.of(
                                List.of(cryptoPay, fiatPay) //
                        ))
                        .build();

                bot.sendMessage(chatId, "Choose your CryptoPay method:",true, cryptoMarkup);

                LabeledPrice price1 = new LabeledPrice("Subscription",6000);

                InlineKeyboardButton runpayButton = new InlineKeyboardButton();
                runpayButton.setText("RunPay Provider");
                runpayButton.setPay(true);

                InlineKeyboardMarkup runpayMarkup = InlineKeyboardMarkup.builder()
                        .keyboard(List.of(List.of(runpayButton)))
                        .build();

                SendInvoice runpay = SendInvoice.builder()
                        .chatId(chatId)
                        .title("RunPay Provider")
                        .description("One time purchase")
                        .payload("purchase number 001")
                        .providerToken(RunPayToken)
                        .currency("MDL")
                        .needEmail(true)
                        .prices(List.of(price1))
                        .needName(true)
                        .needPhoneNumber(true)
                        .needShippingAddress(false)
                        .replyMarkup(runpayMarkup)
                        .startParameter("premium123")
                        .build();

                bot.sendMessage(chatId, "Or pay by your local provider:", true);
                bot.execute(runpay);

            } catch (Exception e) {
                log.error("Something went wrong. Please try again later.");
            }
            return;
        }

        UserState currentState = userStateService.getUserState(chatId);
        if (messageText.startsWith("/")) {
            try {
                commandHandler.handleCommand(messageText, chatId, username);
            } catch (Exception e) {
                bot.sendMessage(chatId, "❌ Something went wrong while executing that command. Please try again.", true);
                log.warn("Command '{}' from user '{}' failed: {}", messageText, username, e.getMessage());
            }
            return;
        }

        if (currentState == null) {
            bot.sendMessage(chatId, "🤖 I'm not sure what you're trying to do. Use /start to see available commands.", true);
            log.info("User '{}' sent unrecognized message: '{}'", username, messageText);
            return;
        }

        try {
            BiConsumer<Long, String> handler = stateHandlers.get(currentState);

            if (handler != null) {
                handler.accept(chatId, messageText);
            } else {
                commandHandler.handleCommand(messageText, chatId, username);
                log.warn("Unhandled state: {}", currentState);
            }
        } catch (Exception e) {
            bot.sendMessage(chatId, "⚠️ Oops! Something went wrong while processing your input.", true);
            log.error("Error processing input in state '{}': {}", currentState, e.getMessage(), e);
        }
    }

    private void handleNewUser(long chatId, String username) {
        if (chatId == 0) return;
        userRepository.insertIfNotExists(chatId,username, LocalDateTime.now());
    }

    private void processCryptoSymbol(long chatId, String symbol) {
        try {
            if (this.userStateService.getUserState(chatId).equals(UserState.WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE)) {
                CryptoData result = cryptoService.retrieveCryptoFullInfo(symbol);
                bot.sendMessage(chatId, result.toString(),false);
            } else if (this.userStateService.getUserState(chatId).equals(UserState.WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE)) {
                JSONObject object = cryptoService.retrieveCryptoPrice(symbol);
                bot.sendMessage(chatId, "Symbol: " + symbol + " \n" +
                        "Price: " + object.getString("price"),false);
            } else if (this.userStateService.getUserState(chatId).equals(UserState.WAITING_FOR_CREATE_ALERT_SYMBOL)) {
                JSONObject object = cryptoService.retrieveCryptoPrice(symbol);
                String currentPrice = object.getString("price");
                bot.sendMessage(chatId, "Symbol: " + symbol + " \n" +
                        "Current Price: " + currentPrice + "\n" +
                        "Please provide the target price",false);
            } else {
                JSONObject object = cryptoService.searchBySymbolOrByName(symbol);
                JSONObject data = object.getJSONObject("Data");
                if (data == null || !data.has("LIST") || data.getJSONArray("LIST").isEmpty()) {
                    bot.sendMessage(chatId, "⚠️ No matches found for the symbol `" + symbol + "`.",true);
                    return;
                }

                JSONArray bestMatchCrypto = data.getJSONArray("LIST");
                JSONObject cryptoMatch = bestMatchCrypto.getJSONObject(0);

                String correctSymbol = cryptoMatch.getString("SYMBOL");
                String correctName = cryptoMatch.getString("NAME");
                String type = cryptoMatch.getString("ASSET_TYPE");
                boolean smartContractCapabilities = cryptoMatch.getBoolean("HAS_SMART_CONTRACT_CAPABILITIES");
                bot.sendMessage(chatId,"Crypto Match Details:\n" +
                        "Symbol: " + correctSymbol + "\n" +
                        "Name: " + correctName + "\n" +
                        "Type: " + type + "\n" +
                        "Smart Contract Capabilities: " + (smartContractCapabilities ? "Yes" : "No"),false);
            }
        } catch (IOException e) {
            log.warn("400 Bad Request - Malformed symbol '{}'", symbol);
            bot.sendMessage(chatId, "⚠️ Invalid symbol format. Please double-check and try again. Format 'TON-USDT'", true);
        } catch (Exception e) {
            bot.sendMessage(chatId, "❌ Something went wrong. Please try again later.", true);
        } finally {
            this.userStateService.clearSession(chatId);
        }
    }


    private void processStockSymbol(long chatId, String symbol) {
        try {
            if (this.userStateService.getUserState(chatId).equals(UserState.WAITING_FOR_STOCK_SYMBOL)) {
                StockData result = stockService.retrieveStockInfo(symbol);
                bot.sendMessage(chatId, result.toString(),false);
            } else {
                List<StockMatch> result = stockService.searchStockBySymbol(symbol);
                String formattedResult = formatStockList(result);
                bot.sendMessage(chatId, formattedResult,false);
            }
        } catch (Exception e) {
            bot.sendMessage(chatId, "⚠️ Could not retrieve data for symbol: `" + symbol + "`\nError: " + e.getMessage(),true);
            log.warn("Error retrieving Stock Data");
        } finally {
            this.userStateService.clearSession(chatId);
        }
    }

    private void processTargetPrice(long chatId, String targetPrice) {
        try {
            if (userStateService.getUserState(chatId) == UserState.WAITING_FOR_CREATE_ALERT_PRICE) {
                double price = Double.parseDouble(targetPrice);
                if (price <= 0) {
                    bot.sendMessage(chatId, "⚠️ Price must be greater than zero. Please try again.",true);
                    return;
                }

                CryptoAlert alert = alertsList.get(chatId);
                alert.setTargetPrice(price);
                alert.setTelegramUserId(chatId);
                alert.setNotified(false);

                cryptoService.createCryptoAlert(chatId, alert.getSymbol(), price);
                bot.sendMessage(chatId, "✅ Alert created for *" + alert.getSymbol() + "* at target price: *" + price + "*", true);
            }
        } catch (NumberFormatException e) {
            bot.sendMessage(chatId, "❌ Invalid input. Please enter a valid number (e.g. 23450.75).",true);
            log.warn("Invalid price input from {}: {}", chatId, targetPrice);
        } catch (Exception e) {
            bot.sendMessage(chatId, "❌ Something went wrong while creating the alert. Please try again.",true);
            log.error("Error creating alert for {}: {}", chatId, e.getMessage(), e);
        } finally {
            userStateService.clearSession(chatId);
            alertsList.remove(chatId);
        }
    }

    private void processDeleteAlert(long chatId, String symbol) {
        try {
            if (userStateService.getUserState(chatId) == UserState.WAITING_FOR_DELETE_ALERT_SYMBOL) {
                boolean deleted = cryptoService.deleteMyAlert(chatId, symbol);

                if (deleted) {
                    bot.sendMessage(chatId, "🗑️ Alert for *" + symbol.toUpperCase() + "* has been successfully deleted.", true);
                } else {
                    bot.sendMessage(chatId, "⚠️ No alert found for *" + symbol.toUpperCase() + "*.", true);
                }
            }
        } catch (Exception e) {
            bot.sendMessage(chatId, "❌ Failed to delete alert for *" + symbol.toUpperCase() + "*.\nPlease try again later.", true);
            log.error("Error deleting alert for user {} and symbol {}: {}", chatId, symbol, e.getMessage(), e);
        } finally {
            this.userStateService.clearSession(chatId);
        }
    }


    private void processFromCurrency(long chatId, String fromCurrency) {
        try {
            if (userStateService.getCurrencySession(chatId) != null &&
                    UserState.WAITING_FOR_FROM_CURRENCY.equals(userStateService.getUserState(chatId))) {

                fromCurrency = fromCurrency.trim().toUpperCase(); // Normalize input
                userStateService.setCurrencySession(chatId, fromCurrency);

                bot.sendMessage(chatId, "✅ You selected *" + fromCurrency + "* as the source currency.\n" +
                        "Now, please provide the _target_ currency symbol (e.g., `EUR`).", true);
            } else {
                bot.sendMessage(chatId, "⚠️ Unexpected input. Please start a new currency exchange session.",true);
            }
        } catch (Exception e) {
            bot.sendMessage(chatId, "❌ Error processing your input: " + e.getMessage(),true);
            log.error("Failed to process 'fromCurrency' for user {}: {}", chatId, e.getMessage(), e);
        }
    }


    private void processToCurrency(long chatId, String toCurrency) {
        if (UserState.WAITING_FOR_TO_CURRENCY.equals(this.userStateService.getUserState(chatId))) {
            try {
                String fromCurrency = userStateService.getCurrencySession(chatId);

                if (fromCurrency != null && !fromCurrency.isBlank()) {
                    toCurrency = toCurrency.trim().toUpperCase();
                    ExchangeRateData exchangeData = forexService.retrieveCurrencyExchangeRate(fromCurrency, toCurrency);
                    bot.sendMessage(chatId, exchangeData.toString(),false);
                } else {
                    bot.sendMessage(chatId, "⚠️ Could not find the source currency. Please restart the exchange process.",true);
                    log.warn("From currency is missing for user {}", chatId);
                }

            } catch (Exception e) {
                bot.sendMessage(chatId, "❌ Error fetching exchange rate: " + e.getMessage(),true);
                log.error("Exchange rate retrieval failed for user {}: {}", chatId, e.getMessage(), e);
            } finally {
                userStateService.clearSession(chatId);
                this.userStateService.clearSession(chatId);
            }
        }
    }
}
