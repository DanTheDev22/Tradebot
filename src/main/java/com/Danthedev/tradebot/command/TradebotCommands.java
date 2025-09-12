package com.Danthedev.tradebot.command;

public class TradebotCommands {

    public static class MarketCommands {
        public static final String STATUS_MARKET = "/status";
        public static final String EXCHANGE_RATE = "/exchange";
        public static final String GET_SUPPORTED_PHYSICAL_CURRENCY = "/suppcurrency";
    }

    public static class CryptoCommands {
        public static final String GET_FULL_CRYPTO = "/getfullcrypto";
        public static final String GET_SIMPLE_CRYPTO = "/getsimplecrypto";
        public static final String FIND_CRYPTO = "/findcrypto";
        public static final String GET_SUPPORTED_DIGITAL_CURRENCY = "/suppcrypto";
    }

    public static class StockCommands {
        public static final String GET_STOCK = "/getstock";
        public static final String FIND_STOCK = "/findstock";
    }

    public static class AlertCommands {
        public static final String CREATE_ALERT = "/createalert";
        public static final String SHOW_ALERTS = "/showalerts";
        public static final String DELETE_ALERT = "/deletealert";
    }

    public static final String START_COMMAND = "/start";
    public static final String DEFAULT_MESSAGE = "Sorry, I didn't understand that. Try one of the available commands.";
    public static final String NO_ACCESS = "To access this service, please pay the subscription fee.";
}
