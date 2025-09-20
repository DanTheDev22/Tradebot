package com.Danthedev.tradebot.domain.service.handler.crypto;

import com.Danthedev.tradebot.UserState;
import com.Danthedev.tradebot.domain.dto.CryptoData;
import com.Danthedev.tradebot.domain.model.CryptoAlert;
import com.Danthedev.tradebot.domain.repository.cryptoAlert.CryptoAlertRepository;
import com.Danthedev.tradebot.domain.service.CryptoService;
import com.Danthedev.tradebot.domain.service.NotificationService;
import com.Danthedev.tradebot.domain.service.UserStateService;
import com.Danthedev.tradebot.domain.service.handler.MessageHandler;
import com.Danthedev.tradebot.telegram.BotSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.IOException;
import java.util.*;

import static com.Danthedev.tradebot.UserState.WAITING_FOR_CREATE_ALERT_PHASE_2;

@Slf4j
@Component
@RequiredArgsConstructor
public class CryptoMessageHandler implements MessageHandler {

    private final BotSender bot;
    private final CryptoService cryptoService;
    private final UserStateService userStateService;
    private final NotificationService notifier;
    private final CryptoAlertRepository alertRepository;

    private final Map<Long, CryptoAlert> alertsList = new HashMap<>();

    @Override
    public void handle(long chatId, String symbol) {
        UserState state = UserState.valueOf(userStateService.getState(chatId));
        try {
            switch (state) {
                case WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE, WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE,
                     WAITING_FOR_CREATE_ALERT_PHASE_1,
                     WAITING_FOR_CRYPTO_SEARCH_SYMBOL -> processCryptoSymbol(chatId, symbol);
                case WAITING_FOR_CREATE_ALERT_PHASE_2 -> processTargetPrice(chatId, symbol);
                case WAITING_FOR_DELETE_ALERT_SYMBOL -> processDeleteAlert(chatId, symbol);
                default -> bot.sendText(chatId, "⚠️ Invalid state for cryptoAlert handler.", true);
            }
        } catch (Exception e) {
            bot.sendText(chatId, "❌ Something went wrong. Please try again later.", true);
            log.error("Error in CryptoMessageHandler: {}", e.getMessage(), e);
        }
    }

    public void processCryptoSymbol(long chatId, String symbol) {
        try {
            if (UserState.valueOf(userStateService.getState(chatId)).equals(UserState.WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE)) {
                CryptoData result = cryptoService.retrieveCryptoFullInfo(symbol);
                bot.sendText(chatId, result.toString(), false);
                this.userStateService.clearSession(chatId);
            } else if (UserState.valueOf(userStateService.getState(chatId)).equals(UserState.WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE)) {
                JSONObject object = cryptoService.retrieveCryptoPrice(symbol);
                bot.sendText(chatId, "Symbol: " + symbol + " \n" +
                        "Price: " + object.getString("price"), false);
                this.userStateService.clearSession(chatId);
            } else if (UserState.valueOf(userStateService.getState(chatId)).equals(UserState.WAITING_FOR_CREATE_ALERT_PHASE_1)) {
                JSONObject object = cryptoService.retrieveCryptoPrice(symbol);
                String currentPrice = object.getString("price");
                bot.sendText(chatId, "Symbol: " + symbol + " \n" +
                        "Current Price: " + currentPrice + "\n" +
                        "Please provide the target price", false);
                CryptoAlert alert = new CryptoAlert();
                alert.setSymbol(symbol);
                alertsList.put(chatId, alert);
                this.userStateService.setState(chatId, WAITING_FOR_CREATE_ALERT_PHASE_2);

            } else {
                JSONObject object = cryptoService.searchBySymbolOrByName(symbol);
                JSONObject data = object.getJSONObject("Data");
                if (data == null || !data.has("LIST") || data.getJSONArray("LIST").isEmpty()) {
                    bot.sendText(chatId, "⚠️ No matches found for the symbol `" + symbol + "`.", true);
                    return;
                }

                JSONArray bestMatchCrypto = data.getJSONArray("LIST");
                JSONObject cryptoMatch = bestMatchCrypto.getJSONObject(0);

                String correctSymbol = cryptoMatch.getString("SYMBOL");
                String correctName = cryptoMatch.getString("NAME");
                String type = cryptoMatch.getString("ASSET_TYPE");
                boolean smartContractCapabilities = cryptoMatch.getBoolean("HAS_SMART_CONTRACT_CAPABILITIES");
                bot.sendText(chatId, "Crypto Match Details:\n" +
                        "Symbol: " + correctSymbol + "\n" +
                        "Name: " + correctName + "\n" +
                        "Type: " + type + "\n" +
                        "Smart Contract Capabilities: " + (smartContractCapabilities ? "Yes" : "No"), false);

                this.userStateService.clearSession(chatId);
            }
        } catch (IOException e) {
            log.warn("400 Bad Request - Malformed symbol '{}'", symbol);
            bot.sendText(chatId, "⚠️ Invalid symbol format. Please double-check and try again. Format 'TON-USDT'", true);
        } catch (Exception e) {
            bot.sendText(chatId, "❌ Something went wrong. Please try again later.", true);
        }
    }

    public void processTargetPrice(long chatId, String targetPrice) {
        try {
            if (UserState.valueOf(userStateService.getState(chatId)) == WAITING_FOR_CREATE_ALERT_PHASE_2) {
                double price = Double.parseDouble(targetPrice);
                if (price <= 0) {
                    bot.sendText(chatId, "⚠️ Price must be greater than zero. Please try again.", true);
                    return;
                }

                CryptoAlert alert = alertsList.get(chatId);

                cryptoService.createCryptoAlert(chatId, alert.getSymbol(), price);
                alertsList.remove(chatId);
                bot.sendText(chatId, "✅ Alert created for *" + alert.getSymbol() + "* at target price: *" + price + "*", true);
                this.userStateService.clearSession(chatId);
            }
        } catch (NumberFormatException e) {
            bot.sendText(chatId, "❌ Invalid input. Please enter a valid number (e.g. 23450.75).", true);
            log.warn("Invalid price input from {}: {}", chatId, targetPrice);
        } catch (Exception e) {
            bot.sendText(chatId, "❌ Something went wrong while creating the invoice. Please try again.", true);
            log.error("Error creating invoice for {}: {}", chatId, e.getMessage(), e);
        }
    }

    private void processDeleteAlert(long chatId, String symbol) {
        try {
            if (UserState.valueOf(userStateService.getState(chatId)) == UserState.WAITING_FOR_DELETE_ALERT_SYMBOL) {

                List<CryptoAlert> alerts = cryptoService.getAlertsForSymbol(chatId, symbol);
                if (alerts.isEmpty()) {
                    bot.sendText(chatId, "⚠️ No alerts found for *" + symbol.toUpperCase() + "*.", true);
                    userStateService.clearSession(chatId);
                    return;
                }

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                for (CryptoAlert alert : alerts) {
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText("Target: " + alert.getTargetPrice());
                    button.setCallbackData("delete_alert:" + alert.getId());
                    rows.add(Collections.singletonList(button));
                }
                markup.setKeyboard(rows);

                bot.sendText(chatId, "Select the alert to delete:", true, markup);

            }
        } catch (Exception e) {
            notifier.sendError(chatId, "❌ Failed to delete alert for *" + symbol.toUpperCase() + "*.\nPlease try again later.");
            log.error("Error deleting alert for user {} and symbol {}: {}", chatId, symbol, e.getMessage(), e);
            userStateService.clearSession(chatId);
        }
    }

    public boolean deleteAlertById(Long chatId, Long alertId) {
        Optional<CryptoAlert> alert = cryptoService.findAlertById(alertId);
        if (alert.isPresent() && alert.get().getTelegramUserId().equals(chatId)) {
            cryptoService.deleteAlert(alert.get());
            return true;
        }
        return false;
    }
}
