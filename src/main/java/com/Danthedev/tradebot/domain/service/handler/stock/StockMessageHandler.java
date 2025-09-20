package com.Danthedev.tradebot.domain.service.handler.stock;

import com.Danthedev.tradebot.UserState;
import com.Danthedev.tradebot.domain.dto.StockData;
import com.Danthedev.tradebot.domain.dto.StockMatch;
import com.Danthedev.tradebot.domain.service.StockService;
import com.Danthedev.tradebot.domain.service.UserStateService;
import com.Danthedev.tradebot.domain.service.handler.MessageHandler;
import com.Danthedev.tradebot.telegram.BotSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.Danthedev.tradebot.domain.dto.StockMatch.formatStockList;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockMessageHandler implements MessageHandler {

    private final BotSender bot;
    private final UserStateService userStateService;
    private final StockService stockService;

    private void processStockSymbol(long chatId, String symbol) {
        try {
            if (UserState.valueOf(userStateService.getState(chatId)).equals(UserState.WAITING_FOR_STOCK_SYMBOL)) {
                StockData result = stockService.retrieveStockInfo(symbol);
                bot.sendText(chatId, result.toString(),false);
            } else {
                List<StockMatch> result = stockService.searchStockBySymbol(symbol);
                String formattedResult = formatStockList(result);
                bot.sendText(chatId, formattedResult,false);
            }
        } catch (Exception e) {
            bot.sendText(chatId, "⚠️ Could not retrieve data for symbol: `" + symbol + "`\nError: " + e.getMessage(),true);
            log.warn("Error retrieving Stock Data");
        } finally {
            this.userStateService.clearSession(chatId);
        }
    }

    @Override
    public void handle(long chatId, String symbol) {
        UserState state = UserState.valueOf(userStateService.getState(chatId));
        try {
            if (Objects.requireNonNull(state) == UserState.WAITING_FOR_STOCK_SYMBOL || Objects.requireNonNull(state) == UserState.WAITING_FOR_STOCK_SEARCH_SYMBOL ) {
                processStockSymbol(chatId, symbol);
            } else {
                bot.sendText(chatId, "⚠️ Invalid state for stock handler.", true);
            }
        } catch (Exception e) {
            bot.sendText(chatId, "❌ Something went wrong. Please try again later.", true);
            log.error("Error in StockMessageHandler: {}", e.getMessage(), e);
        } finally {
            userStateService.clearSession(chatId);
        }
    }
}
