package com.Danthedev.Tradebot.domain.service;

import com.Danthedev.Tradebot.UserState;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserStateService {
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private final Map<Long, String> currencySessions = new HashMap<>();

    public void setUserState(long chatId, UserState state) {
        userStates.put(chatId, state);
    }

    public UserState getUserState(long chatId) {
        return userStates.get(chatId);
    }

    public void setCurrencySession(long chatId, String currency) {
        currencySessions.put(chatId, currency);
    }

    public String getCurrencySession(long chatId) {
        return currencySessions.get(chatId);
    }

    public void clearSession(long chatId) {
        userStates.remove(chatId);
        currencySessions.remove(chatId);
    }
}

