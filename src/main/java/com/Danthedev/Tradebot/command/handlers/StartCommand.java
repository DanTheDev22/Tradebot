package com.Danthedev.Tradebot.command.handlers;

import com.Danthedev.Tradebot.command.Command;
import com.Danthedev.Tradebot.command.TradebotCommands;
import com.Danthedev.Tradebot.telegram.BotSenderImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartCommand implements Command {

    private final BotSenderImpl bot;

    @Override
    public String getCommand() {
        return TradebotCommands.START_COMMAND;
    }

    @Override
    public void execute(long chatId, String username) {
        String message = "Hi " + username + "! I am TradeBot and I will be your financial assistant! \n" +
                "\n Use the following commands: \n" +
                "/status - shows the US market status \n" +
                "/suppcrypto - returns a list with all CryptoCurrencies supported for Exchange \n" +
                "/suppcurrency - returns a list with all Physical Currencies supported for Exchange \n" +
                "/getsimplecrypto - shows the price of a cryptocurrency \n" +
                "/getfullcrypto - shows  full details about a cryptocurrency \n" +
                "/findcrypto - returns the best-matching symbols and market information for cryptocurrency \n" +
                "/getstock - shows  full details about an stock \n" +
                "/findstock - returns the best-matching symbols and market information for stock \n" +
                "/exchange - returns the realtime exchange rate for a pair of digital currency (e.g., Bitcoin) or physical currency (e.g., USD). \n" +
                "/createalert - creates alert price and gives notification when is reached \n" +
                "/showalerts - shows user's alerts \n" +
                "/deletealert - delete user's alert \n";
        bot.sendText(chatId, message,false);
    }

    @Override
    public String getCommandName() {
        return "";
    }
}
