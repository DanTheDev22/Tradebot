package com.Danthedev.tradebot.domain.service.handler;

public interface MessageHandler {

     void handle(long chatId, String symbol);
}
