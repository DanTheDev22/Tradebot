package com.Danthedev.Tradebot.service;

import com.Danthedev.Tradebot.BotSenderImpl;
import com.Danthedev.Tradebot.UserState;
import com.Danthedev.Tradebot.model.CryptoAlert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.File;
import java.util.List;

import static com.Danthedev.Tradebot.TradebotCommands.AlertCommands.*;
import static com.Danthedev.Tradebot.TradebotCommands.CryptoCommands.*;
import static com.Danthedev.Tradebot.TradebotCommands.CryptoCommands.FIND_CRYPTO;
import static com.Danthedev.Tradebot.TradebotCommands.DEFAULT_MESSAGE;
import static com.Danthedev.Tradebot.TradebotCommands.MarketCommands.*;
import static com.Danthedev.Tradebot.TradebotCommands.START_COMMAND;
import static com.Danthedev.Tradebot.TradebotCommands.StockCommands.FIND_STOCK;
import static com.Danthedev.Tradebot.TradebotCommands.StockCommands.GET_STOCK;
import static com.Danthedev.Tradebot.model.CryptoAlert.formatCryptoAlertList;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandHandlerImpl implements CommandHandler{

    private final BotSenderImpl bot;
    private final MarketStatusService marketStatusService;
    private final CryptoService cryptoService;
    private final UserStateService userState;

    @Override
    public void handleCommand(String messageText, long chatId, String username) {
        switch (messageText) {
            case START_COMMAND -> startCommandReceived(chatId, username);
            case STATUS_MARKET -> handleStatusCommand(chatId);
            case GET_SUPPORTED_DIGITAL_CURRENCY -> handleGetSupportedDigitalCurrency(chatId);
            case GET_SUPPORTED_PHYSICAL_CURRENCY -> handleGetSupportedPhysicalCurrency(chatId);
            case GET_FULL_CRYPTO -> handleGetFullCryptoResponseCommand(chatId);
            case GET_SIMPLE_CRYPTO -> handleGetSimpleCryptoResponseCommand(chatId);
            case FIND_CRYPTO -> handleSearchBySymbolOrByNameCryptoCommand(chatId);
            case GET_STOCK -> handleGetStockResponseCommand(chatId);
            case FIND_STOCK -> handleFindBySymbolCommand(chatId);
            case EXCHANGE_RATE -> handleExchangeCommand(chatId);
            case CREATE_ALERT -> handleCreateAlertCommand(chatId);
            case SHOW_ALERTS -> handleShowAlertsCommand(chatId);
            case DELETE_ALERT -> handleDeleteAlertCommand(chatId);
            default -> bot.sendMessage(chatId, DEFAULT_MESSAGE,false);
        }
    }

    private void startCommandReceived(Long chatId, String username) {
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
        bot.sendMessage(chatId, message,false);
    }

    private void handleStatusCommand(long chatId) {
        try {
            String result = marketStatusService.getMarketStatus();
            bot.sendMessage(chatId, result,false);
        } catch (Exception e) {
            bot.sendMessage(chatId, "❌ Unable to retrieve the market status right now. Please try again later.",true);
            log.warn("❗ Error retrieving market status for chatId {}: {}", chatId, e.getMessage(), e);
        }
    }

    private void handleGetSupportedDigitalCurrency(long chatId) {
        InputFile file = new InputFile(new File("src/main/resources/digital_currency_list.txt"));
        bot.sendDocument(chatId,file);
    }

    private void handleGetSupportedPhysicalCurrency(long chatId) {
        InputFile file = new InputFile(new File("src/main/resources/physical_currency_list.txt"));
        bot.sendDocument(chatId,file);
    }

    private void handleGetFullCryptoResponseCommand(long chatId) {
        userState.setUserState(chatId, UserState.WAITING_FOR_CRYPTO_SYMBOL_FULL_RESPONSE);
        bot.sendMessage(chatId, "Please provide a symbol. Example TON-USDT",false);
    }

    private void handleGetSimpleCryptoResponseCommand(long chatId) {
        userState.setUserState(chatId, UserState.WAITING_FOR_CRYPTO_SYMBOL_SIMPLE_RESPONSE);
        bot.sendMessage(chatId, "Please provide a symbol. Example TON-USDT",false);
    }

    private void handleSearchBySymbolOrByNameCryptoCommand(long chatId) {
        userState.setUserState(chatId, UserState.WAITING_FOR_CRYPTO_SEARCH_SYMBOL);
        bot.sendMessage(chatId, "Please provide a symbol or a name for crypto",false);
    }

    private void handleGetStockResponseCommand(long chatId) {
        userState.setUserState(chatId, UserState.WAITING_FOR_STOCK_SYMBOL);
        bot.sendMessage(chatId, "Please provide a symbol. Example TSLA",false);
    }

    private void handleFindBySymbolCommand(long chatId) {
        userState.setUserState(chatId, UserState.WAITING_FOR_STOCK_SEARCH_SYMBOL);
        bot.sendMessage(chatId, "Please provide a symbol or a name for stock",false);
    }

    private void handleCreateAlertCommand(long chatId) {
        userState.setUserState(chatId, UserState.WAITING_FOR_CREATE_ALERT_SYMBOL);
        bot.sendMessage(chatId, "Please provide a symbol for cryptocurrency. Example TON-USDT",false);
    }

    private void handleExchangeCommand(long chatId) {
        userState.setUserState(chatId, UserState.WAITING_FOR_FROM_CURRENCY);
        userState.setCurrencySession(chatId, null);

        String message = """
                Please provide the symbol for the currency/digital currency \
                you want to exchange from (e.g., USD, EUR, BTC, ETH).
                Check supported Digital Currencies - /suppcrypto\s
                Check supported Physical Currencies - /suppcurrency\s
                """;

        bot.sendMessage(chatId, message,false);
    }

    private void handleShowAlertsCommand(long chatId) {
        List<CryptoAlert> foundAlerts = cryptoService.showAllMyAlerts(chatId);
        bot.sendMessage(chatId, formatCryptoAlertList(foundAlerts),false);
    }

    private void handleDeleteAlertCommand(long chatId) {
        userState.setUserState(chatId, UserState.WAITING_FOR_DELETE_ALERT_SYMBOL);
        bot.sendMessage(chatId, "Please provide a symbol for cryptocurrency. Example TON-USDT",false);
    }
}
