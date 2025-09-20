package com.Danthedev.tradebot.command.handlers.forex;

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
public class ExchangeCommand implements Command {

    private final BotSender bot;
    private final UserStateService userState;

    @Override
    public String getCommand() {
        return TradebotCommands.MarketCommands.EXCHANGE_RATE;
    }

    @Override
    public void execute(long chatId, String username) {
        userState.setState(chatId, UserState.WAITING_FOR_FROM_CURRENCY);
        String message = """
                Please provide the symbol for the currency/digital currency \
                you want to exchange from (e.g., USD, EUR, BTC, ETH).
                Check supported Digital Currencies - /suppcrypto\s
                Check supported Physical Currencies - /suppcurrency\s
                """;

        bot.sendText(chatId, message,false);
    }
}
