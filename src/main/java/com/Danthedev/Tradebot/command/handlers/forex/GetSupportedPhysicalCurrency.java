package com.Danthedev.Tradebot.command.handlers.forex;

import com.Danthedev.Tradebot.command.Command;
import com.Danthedev.Tradebot.command.TradebotCommands;
import com.Danthedev.Tradebot.telegram.BotSenderImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.File;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetSupportedPhysicalCurrency implements Command {

    private final BotSenderImpl bot;

    @Override
    public String getCommand() {
        return TradebotCommands.MarketCommands.GET_SUPPORTED_PHYSICAL_CURRENCY;
    }

    @Override
    public void execute(long chatId, String username) {
        InputFile file = new InputFile(new File("src/main/resources/physical_currency_list.txt"));
        bot.sendDocument(chatId,file);
    }

    @Override
    public String getCommandName() {
        return "";
    }
}
