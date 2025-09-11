package com.Danthedev.Tradebot.command;

public interface Command {

     String getCommand();

     void execute(long chatId, String username);

     String getCommandName();
}
