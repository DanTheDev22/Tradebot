package com.Danthedev.Tradebot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@AllArgsConstructor
public class StockInfo {

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
        // Converting string values to BigDecimal
        BigDecimal indexPrice = new BigDecimal(price);
        BigDecimal previousClosePrice = new BigDecimal(previousClose);

        // Calculating price change and change percentage
        BigDecimal changeValue = indexPrice.subtract(previousClosePrice);
        BigDecimal changePercent = changeValue.divide(previousClosePrice, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        // Returning formatted string
        return "Symbol: " + symbol + "\n" +
                "Price: " + price + "$\n" +
                String.format("Price Change: %.2f$\n", changeValue) +
                String.format("Price Change %%: %.2f%%\n", changePercent) +
                "High: " + high + "$\n" +
                "Low: " + low + "$\n" +
                "Open: " + open + "$\n" +
                "Latest Trading Day: " + latest_trading_day + "\n" +
                "Previous Close: " + previousClose + "$";
    }
}
