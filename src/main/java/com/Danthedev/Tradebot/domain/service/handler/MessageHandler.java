package com.Danthedev.Tradebot.domain.service.handler;

public interface MessageHandler {

     void handle(long chatId, String symbol);
}
