package com.Danthedev.tradebot.command;

import com.Danthedev.tradebot.telegram.BotSenderImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class CommandHandlerImpl implements CommandHandler{

    private final List<Command> commands;
    private final Map<String, Command> commandMap;
    private final BotSenderImpl bot;

    @PostConstruct
    public void init() {
        for (Command command : commands) {
            commandMap.put(command.getCommand(), command);
        }
        System.out.println(commands);
        System.out.println(commandMap);
        log.info("Registered {} bot commands", commandMap.size());
    }

    @Override
    public void handleCommand(String messageText, long chatId, String username) {
        String commandKey = messageText.split(" ")[0].trim();

        Command command = commandMap.get(commandKey);
        if (command != null) {
            command.execute(chatId, username);
        } else {
            bot.sendText(chatId, TradebotCommands.DEFAULT_MESSAGE, false);
        }
    }
}
