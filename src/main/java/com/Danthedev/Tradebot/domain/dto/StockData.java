package com.Danthedev.Tradebot.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockData {
    private static final int PRICE_PRECISION = 2;
    private static final int PERCENT_PRECISION = 2;

    private String symbol;
    private String open;
    private String high;
    private String low;
    private String price;
    private String volume;
    private String latest_trading_day;
    private String previousClose;
    private String change;
    private String change_percent;

    @Override
    public String toString() {
        double indexPrice = price != null ? Double.parseDouble(price) : 0.0;
        double previousClosePrice = previousClose != null ? Double.parseDouble(previousClose) : 0.0;

        double changeValue = indexPrice - previousClosePrice;
        double changePercent = (changeValue / previousClosePrice) * 100;

        return String.format(
                "Symbol: %s\n" +
                        "Price: %s$\n" +
                        "Price Change: %." + PRICE_PRECISION + "f$\n" +
                        "Price Change %%: %." + PERCENT_PRECISION + "f%%\n" +
                        "High: %s$\n" +
                        "Low: %s$\n" +
                        "Open: %s$\n" +
                        "Latest Trading Day: %s\n" +
                        "Previous Close: %s$",
                symbol,
                price,
                changeValue,
                changePercent,
                high,
                low,
                open,
                latest_trading_day,
                previousClose
        );
    }
}
