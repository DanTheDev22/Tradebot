package com.Danthedev.Tradebot.command.handlers.forex;

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
public class ExchangeCommand implements Command {

    private final BotSenderImpl bot;
    private final UserStateService userState;

    @Override
    public String getCommand() {
        return TradebotCommands.MarketCommands.EXCHANGE_RATE;
    }

    @Override
    public void execute(long chatId, String username) {
        userState.setUserState(chatId, UserState.WAITING_FOR_FROM_CURRENCY);
        userState.setCurrencySession(chatId, null);

        String message = """
                Please provide the symbol for the currency/digital currency \
                you want to exchange from (e.g., USD, EUR, BTC, ETH).
                Check supported Digital Currencies - /suppcrypto\s
                Check supported Physical Currencies - /suppcurrency\s
                """;

        bot.sendText(chatId, message,false);
    }

    @Override
    public String getCommandName() {
        return "";
    }
}
