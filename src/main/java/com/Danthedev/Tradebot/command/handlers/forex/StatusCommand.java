package com.Danthedev.Tradebot.command.handlers.forex;


import com.Danthedev.Tradebot.command.Command;
import com.Danthedev.Tradebot.command.TradebotCommands;
import com.Danthedev.Tradebot.domain.service.MarketStatusService;
import com.Danthedev.Tradebot.telegram.BotSenderImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusCommand implements Command {

    private final BotSenderImpl bot;
    private final MarketStatusService marketStatusService;

    @Override
    public String getCommand() {
        return TradebotCommands.MarketCommands.STATUS_MARKET;
    }

    @Override
    public void execute(long chatId, String username) {
        try {
            String result = marketStatusService.getMarketStatus();
            bot.sendText(chatId, result,false);
        } catch (Exception e) {
            bot.sendText(chatId, "❌ Unable to retrieve the market status right now. Please try again later.",true);
            log.warn("❗ Error retrieving market status for chatId {}: {}", chatId, e.getMessage(), e);
        }
    }

    @Override
    public String getCommandName() {
        return "";
    }
}
