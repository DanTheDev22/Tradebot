package com.Danthedev.Tradebot.command.handlers.alerts;

import com.Danthedev.Tradebot.UserState;
import com.Danthedev.Tradebot.command.Command;
import com.Danthedev.Tradebot.command.TradebotCommands;
import com.Danthedev.Tradebot.domain.service.UserStateService;
import com.Danthedev.Tradebot.telegram.BotSenderImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateAlertCommand implements Command {

    private final BotSenderImpl bot;
    private final UserStateService userState;

    @Override
    public String getCommand() {
        return TradebotCommands.AlertCommands.CREATE_ALERT;
    }

    @Override
    public void execute(long chatId, String username) {
        userState.setUserState(chatId, UserState.WAITING_FOR_CREATE_ALERT_SYMBOL);
        bot.sendText(chatId, "Please provide a symbol for cryptocurrency. Example TON-USDT",false);
    }

    @Override
    public String getCommandName() {
        return "";
    }
}
