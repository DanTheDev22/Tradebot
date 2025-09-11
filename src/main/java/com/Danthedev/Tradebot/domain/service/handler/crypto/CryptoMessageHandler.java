package com.Danthedev.Tradebot.domain.service.handler.crypto;

import com.Danthedev.Tradebot.UserState;
import com.Danthedev.Tradebot.domain.dto.CryptoData;
import com.Danthedev.Tradebot.domain.model.CryptoAlert;
import com.Danthedev.Tradebot.domain.service.CryptoService;
import com.Danthedev.Tradebot.domain.service.NotificationService;
import com.Danthedev.Tradebot.domain.service.UserStateService;
import com.Danthedev.Tradebot.domain.service.handler.MessageHandler;
import com.Danthedev.Tradebot.telegram.BotSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CryptoMessageHandler implements MessageHandler {

    private final BotSender bot;
    private final CryptoService cryptoService;
    private final UserStateService userStateService;
    private final NotificationService notifier;


    private final Map<Long, CryptoAlert> alertsList = new HashMap<>();

    @Override
    public void handle(long chatId, String symbol) {
        UserState state = userStateService.getUserState(chatId);
        try {
            switch (state) {
                case WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE, WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE,
                     WAITING_FOR_CREATE_ALERT_PRICE,
                     WAITING_FOR_CRYPTO_SEARCH_SYMBOL -> processCryptoSymbol(chatId, symbol);
                case WAITING_FOR_CREATE_ALERT_SYMBOL -> processTargetPrice(chatId,symbol);
                case WAITING_FOR_DELETE_ALERT_SYMBOL -> processDeleteAlert(chatId,symbol);
                default -> bot.sendText(chatId, "⚠️ Invalid state for crypto handler.", true);
            }
        } catch (Exception e) {
            bot.sendText(chatId, "❌ Something went wrong. Please try again later.", true);
            log.error("Error in CryptoMessageHandler: {}", e.getMessage(), e);
        } finally {
            userStateService.clearSession(chatId);
        }
    }

    public void processCryptoSymbol(long chatId, String symbol) {
        try {
            if (this.userStateService.getUserState(chatId).equals(UserState.WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE)) {
                CryptoData result = cryptoService.retrieveCryptoFullInfo(symbol);
                bot.sendText(chatId, result.toString(),false);
            } else if (this.userStateService.getUserState(chatId).equals(UserState.WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE)) {
                JSONObject object = cryptoService.retrieveCryptoPrice(symbol);
                bot.sendText(chatId, "Symbol: " + symbol + " \n" +
                        "Price: " + object.getString("price"),false);
            } else if (this.userStateService.getUserState(chatId).equals(UserState.WAITING_FOR_CREATE_ALERT_SYMBOL)) {
                JSONObject object = cryptoService.retrieveCryptoPrice(symbol);
                String currentPrice = object.getString("price");
                bot.sendText(chatId, "Symbol: " + symbol + " \n" +
                        "Current Price: " + currentPrice + "\n" +
                        "Please provide the target price",false);
            } else {
                JSONObject object = cryptoService.searchBySymbolOrByName(symbol);
                JSONObject data = object.getJSONObject("Data");
                if (data == null || !data.has("LIST") || data.getJSONArray("LIST").isEmpty()) {
                    bot.sendText(chatId, "⚠️ No matches found for the symbol `" + symbol + "`.",true);
                    return;
                }

                JSONArray bestMatchCrypto = data.getJSONArray("LIST");
                JSONObject cryptoMatch = bestMatchCrypto.getJSONObject(0);

                String correctSymbol = cryptoMatch.getString("SYMBOL");
                String correctName = cryptoMatch.getString("NAME");
                String type = cryptoMatch.getString("ASSET_TYPE");
                boolean smartContractCapabilities = cryptoMatch.getBoolean("HAS_SMART_CONTRACT_CAPABILITIES");
                bot.sendText(chatId,"Crypto Match Details:\n" +
                        "Symbol: " + correctSymbol + "\n" +
                        "Name: " + correctName + "\n" +
                        "Type: " + type + "\n" +
                        "Smart Contract Capabilities: " + (smartContractCapabilities ? "Yes" : "No"),false);
            }
        } catch (IOException e) {
            log.warn("400 Bad Request - Malformed symbol '{}'", symbol);
            bot.sendText(chatId, "⚠️ Invalid symbol format. Please double-check and try again. Format 'TON-USDT'", true);
        } catch (Exception e) {
            bot.sendText(chatId, "❌ Something went wrong. Please try again later.", true);
        } finally {
            this.userStateService.clearSession(chatId);
        }
    }

    public void processTargetPrice(long chatId, String targetPrice) {
        try {
            if (userStateService.getUserState(chatId) == UserState.WAITING_FOR_CREATE_ALERT_PRICE) {
                double price = Double.parseDouble(targetPrice);
                if (price <= 0) {
                    bot.sendText(chatId, "⚠️ Price must be greater than zero. Please try again.",true);
                    return;
                }

                CryptoAlert alert = alertsList.get(chatId);
                alert.setTargetPrice(price);
                alert.setTelegramUserId(chatId);
                alert.setNotified(false);

                cryptoService.createCryptoAlert(chatId, alert.getSymbol(), price);
                bot.sendText(chatId, "✅ Alert created for *" + alert.getSymbol() + "* at target price: *" + price + "*", true);
            }
        } catch (NumberFormatException e) {
            bot.sendText(chatId, "❌ Invalid input. Please enter a valid number (e.g. 23450.75).",true);
            log.warn("Invalid price input from {}: {}", chatId, targetPrice);
        } catch (Exception e) {
            bot.sendText(chatId, "❌ Something went wrong while creating the alert. Please try again.",true);
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
                    notifier.sendConfirmation(chatId, "🗑️ Alert for *" + symbol.toUpperCase() + "* has been successfully deleted.");
                } else {
                    bot.sendText(chatId, "⚠️ No alert found for *" + symbol.toUpperCase() + "*.", true);
                }
            }
        } catch (Exception e) {
            notifier.sendError(chatId, "❌ Failed to delete alert for *" + symbol.toUpperCase() + "*.\nPlease try again later.");
            log.error("Error deleting alert for user {} and symbol {}: {}", chatId, symbol, e.getMessage(), e);
        } finally {
            this.userStateService.clearSession(chatId);
        }
    }
}
