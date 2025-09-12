package com.Danthedev.tradebot.command.handlers.alerts;

import com.Danthedev.tradebot.UserState;
import com.Danthedev.tradebot.command.Command;
import com.Danthedev.tradebot.command.TradebotCommands;
import com.Danthedev.tradebot.domain.service.UserStateService;
import com.Danthedev.tradebot.telegram.BotSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateAlertCommand implements Command {

    private final BotSender bot;
    private final UserStateService userState;

    @Override
    public String getCommand() {
        return TradebotCommands.AlertCommands.CREATE_ALERT;
    }

    @Override
    public void execute(long chatId, String username) {
        userState.setState(chatId, UserState.WAITING_FOR_CREATE_ALERT_SYMBOL);
        bot.sendText(chatId, "Please provide a symbol for cryptocurrency. Example TON-USDT",false);
    }
}
