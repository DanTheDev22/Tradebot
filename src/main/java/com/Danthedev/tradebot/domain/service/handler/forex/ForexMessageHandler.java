package com.Danthedev.tradebot.domain.service.handler.forex;

import com.Danthedev.tradebot.UserState;
import com.Danthedev.tradebot.domain.dto.ExchangeRateData;
import com.Danthedev.tradebot.domain.service.ForexService;
import com.Danthedev.tradebot.domain.service.UserStateService;
import com.Danthedev.tradebot.domain.service.handler.MessageHandler;
import com.Danthedev.tradebot.telegram.BotSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForexMessageHandler implements MessageHandler {

    private final BotSender bot;
    private final UserStateService userStateService;
    private final ForexService forexService;

    private final Map<Long, String> fromCurrencyMap = new ConcurrentHashMap<>();

    private void processFromCurrency(long chatId, String fromCurrency) {
        try {
            if (UserState.WAITING_FOR_FROM_CURRENCY.equals(UserState.valueOf(userStateService.getState(chatId)))) {

                fromCurrency = fromCurrency.trim().toUpperCase(); // Normalize input
                userStateService.setState(chatId, UserState.WAITING_FOR_TO_CURRENCY);
                fromCurrencyMap.put(chatId,fromCurrency);

                bot.sendText(chatId, "✅ You selected *" + fromCurrency + "* as the source currency.\n" +
                        "Now, please provide the _target_ currency symbol (e.g., `EUR`).", true);
            } else {
                bot.sendText(chatId, "⚠️ Unexpected input. Please start a new currency exchange session.",true);
            }
        } catch (Exception e) {
            bot.sendText(chatId, "❌ Error processing your input: " + e.getMessage(),true);
            log.error("Failed to process 'fromCurrency' for user {}: {}", chatId, e.getMessage(), e);
        }
    }

    private void processToCurrency(long chatId, String toCurrency) {
        if (UserState.WAITING_FOR_TO_CURRENCY.equals(UserState.valueOf(userStateService.getState(chatId)))) {
            try {
                String fromCurrency = fromCurrencyMap.get(chatId);

                if (fromCurrency != null && !fromCurrency.isBlank()) {
                    toCurrency = toCurrency.trim().toUpperCase();
                    ExchangeRateData exchangeData = forexService.retrieveCurrencyExchangeRate(fromCurrency, toCurrency);
                    bot.sendText(chatId, exchangeData.toString(),false);
                } else {
                    bot.sendText(chatId, "⚠️ Could not find the source currency. Please restart the exchange process.",true);
                    log.warn("From currency is missing for user {}", chatId);
                }

            } catch (Exception e) {
                bot.sendText(chatId, "❌ Error fetching exchange rate: " + e.getMessage(),true);
                log.error("Exchange rate retrieval failed for user {}: {}", chatId, e.getMessage(), e);
            } finally {
                userStateService.clearSession(chatId);
            }
        }
    }

    @Override
    public void handle(long chatId, String symbol) {
        UserState state = UserState.valueOf(userStateService.getState(chatId));
        try {
            switch (state) {
                case WAITING_FOR_FROM_CURRENCY -> processFromCurrency(chatId,symbol);
                case WAITING_FOR_TO_CURRENCY -> processToCurrency(chatId,symbol);
                default -> bot.sendText(chatId, "⚠️ Invalid state for cryptoAlert handler.", true);
            }
        } catch (Exception e) {
            bot.sendText(chatId, "❌ Something went wrong. Please try again later.", true);
            log.error("Error in ForexMessageHandler: {}", e.getMessage(), e);
        }
    }
}
