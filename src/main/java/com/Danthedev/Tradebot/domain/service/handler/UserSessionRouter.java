package com.Danthedev.Tradebot.domain.service.handler;

import com.Danthedev.Tradebot.UserState;
import com.Danthedev.Tradebot.domain.service.UserStateService;
import com.Danthedev.Tradebot.domain.service.handler.crypto.CryptoMessageHandler;
import com.Danthedev.Tradebot.domain.service.handler.forex.ForexMessageHandler;
import com.Danthedev.Tradebot.domain.service.handler.stock.StockMessageHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserSessionRouter {

    private final UserStateService userStateService;

    private final CryptoMessageHandler cryptoHandler;
    private final StockMessageHandler stockHandler;
    private final ForexMessageHandler forexHandler;

    private final Map<UserState, MessageHandler> stateToHandler = new HashMap<>();

    @PostConstruct
    public void init() {
        // Crypto states
        stateToHandler.put(UserState.WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE, cryptoHandler);
        stateToHandler.put(UserState.WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE, cryptoHandler);
        stateToHandler.put(UserState.WAITING_FOR_CRYPTO_SEARCH_SYMBOL, cryptoHandler);
        stateToHandler.put(UserState.WAITING_FOR_CREATE_ALERT_SYMBOL, cryptoHandler);
        stateToHandler.put(UserState.WAITING_FOR_CREATE_ALERT_PRICE, cryptoHandler);
        stateToHandler.put(UserState.WAITING_FOR_DELETE_ALERT_SYMBOL, cryptoHandler);

        // Stock states
        stateToHandler.put(UserState.WAITING_FOR_STOCK_SYMBOL, stockHandler);
        stateToHandler.put(UserState.WAITING_FOR_STOCK_SEARCH_SYMBOL, stockHandler);

        // Forex states
        stateToHandler.put(UserState.WAITING_FOR_FROM_CURRENCY, forexHandler);
        stateToHandler.put(UserState.WAITING_FOR_TO_CURRENCY, forexHandler);
    }

    public boolean route(long chatId, String messageText) {
        UserState currentState = userStateService.getUserState(chatId);
        MessageHandler handler = stateToHandler.get(currentState);

        if (handler != null) {
            handler.handle(chatId, messageText);
            return true;
        }

        return false;
    }
}
