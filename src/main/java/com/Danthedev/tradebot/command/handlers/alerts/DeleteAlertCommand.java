package com.Danthedev.tradebot.command.handlers.alerts;

import com.Danthedev.tradebot.UserState;
import com.Danthedev.tradebot.command.Command;
import com.Danthedev.tradebot.command.TradebotCommands;
import com.Danthedev.tradebot.domain.service.UserStateService;
import com.Danthedev.tradebot.telegram.BotSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteAlertCommand implements Command {

    private final BotSender bot;
    private final UserStateService userState;

    @Override
    public String getCommand() {
        return TradebotCommands.AlertCommands.DELETE_ALERT;
    }

    @Override
    public void execute(long chatId, String username) {
        userState.setState(chatId, UserState.WAITING_FOR_DELETE_ALERT_SYMBOL);
        bot.sendText(chatId, "Please provide a symbol for cryptocurrency. Example TON-USDT",false);
    }
}
