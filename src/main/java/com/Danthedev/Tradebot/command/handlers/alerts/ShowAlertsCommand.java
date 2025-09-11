package com.Danthedev.Tradebot.command.handlers.alerts;

import com.Danthedev.Tradebot.command.Command;
import com.Danthedev.Tradebot.command.TradebotCommands;
import com.Danthedev.Tradebot.domain.model.CryptoAlert;
import com.Danthedev.Tradebot.domain.service.CryptoService;
import com.Danthedev.Tradebot.telegram.BotSenderImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.Danthedev.Tradebot.domain.model.CryptoAlert.formatCryptoAlertList;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShowAlertsCommand implements Command {

    private final BotSenderImpl bot;
    private final CryptoService cryptoService;

    @Override
    public String getCommand() {
        return TradebotCommands.AlertCommands.SHOW_ALERTS;
    }

    @Override
    public void execute(long chatId, String username) {
        List<CryptoAlert> foundAlerts = cryptoService.showAllMyAlerts(chatId);
        bot.sendText(chatId, formatCryptoAlertList(foundAlerts),false);
    }

    @Override
    public String getCommandName() {
        return "";
    }
}
