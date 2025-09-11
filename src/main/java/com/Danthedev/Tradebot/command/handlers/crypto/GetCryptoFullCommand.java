package com.Danthedev.Tradebot.command.handlers.crypto;

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
public class GetCryptoFullCommand implements Command {

    private final BotSenderImpl bot;
    private final UserStateService userState;

    @Override
    public String getCommand() {
        return TradebotCommands.CryptoCommands.GET_FULL_CRYPTO;
    }

    @Override
    public void execute(long chatId, String username) {
        userState.setUserState(chatId, UserState.WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE);
        bot.sendText(chatId, "Please provide a symbol. Example TON-USDT",false);

    }

    @Override
    public String getCommandName() {
        return "";
    }
}
