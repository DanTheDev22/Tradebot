package com.Danthedev.tradebot.command.handlers.alerts;

import com.Danthedev.tradebot.command.Command;
import com.Danthedev.tradebot.command.TradebotCommands;
import com.Danthedev.tradebot.domain.model.CryptoAlert;
import com.Danthedev.tradebot.domain.service.CryptoService;
import com.Danthedev.tradebot.telegram.BotSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.Danthedev.tradebot.domain.model.CryptoAlert.formatCryptoAlertList;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShowAlertsCommand implements Command {

    private final BotSender bot;
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
}
