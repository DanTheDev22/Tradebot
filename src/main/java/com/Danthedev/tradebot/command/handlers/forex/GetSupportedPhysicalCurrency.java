package com.Danthedev.tradebot.command.handlers.forex;

import com.Danthedev.tradebot.command.Command;
import com.Danthedev.tradebot.command.TradebotCommands;
import com.Danthedev.tradebot.telegram.BotSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetSupportedPhysicalCurrency implements Command {

    private final BotSender bot;

    @Override
    public String getCommand() {
        return TradebotCommands.MarketCommands.GET_SUPPORTED_PHYSICAL_CURRENCY;
    }

    @Override
    public void execute(long chatId, String username) {
        String file = "physical_currency_list.txt";
        bot.sendDocument(chatId,file);
    }
}
