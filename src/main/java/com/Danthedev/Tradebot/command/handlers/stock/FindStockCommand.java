package com.Danthedev.Tradebot.command.handlers.stock;

import com.Danthedev.Tradebot.UserState;
import com.Danthedev.Tradebot.command.Command;
import com.Danthedev.Tradebot.command.TradebotCommands;
import com.Danthedev.Tradebot.domain.service.UserStateService;
import com.Danthedev.Tradebot.telegram.BotSenderImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FindStockCommand implements Command {

    private final BotSenderImpl bot;
    private final UserStateService userState;

    @Override
    public String getCommand() {
        return TradebotCommands.StockCommands.FIND_STOCK;
    }

    @Override
    public void execute(long chatId, String username) {
        userState.setUserState(chatId, UserState.WAITING_FOR_STOCK_SEARCH_SYMBOL);
        bot.sendText(chatId, "Please provide a symbol or a name for stock",false);
    }

    @Override
    public String getCommandName() {
        return "";
    }
}
