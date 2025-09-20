package com.Danthedev.tradebot.domain.service;

import com.Danthedev.tradebot.UserState;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Service
public class UserStateService {

    private final RedisTemplate<String, Object> redisTemplate;

    public UserStateService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setState(long chatId, UserState state) {
        redisTemplate.opsForValue().set("user:" + chatId + ":state",state);
    }

    public String getState(long chatId) {
        Object state = redisTemplate.opsForValue().get("user:" + chatId + ":state");
        return state.toString();
    }

    public void clearSession(long chatId) {
        redisTemplate.delete("user:" + chatId + ":state");
    }
}

