package com.Danthedev.tradebot.command;

public interface Command {

     String getCommand();

     void execute(long chatId, String username);
}
