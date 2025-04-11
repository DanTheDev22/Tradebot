package com.Danthedev.Tradebot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StockMatch {

    private String symbol;
    private String name;
    private String type;
    private String region;
    private String marketOpen;
    private String marketClose;
    private String timezone;
    private String currency;
    private String matchScore;

    @Override
    public String toString() {
        return String.format(
                "Symbol: %s\n" +
                        "Name: %s\n" +
                        "Type: %s\n" +
                        "Region: %s\n" +
                        "Market Open: %s\n" +
                        "Market Close: %s\n" +
                        "Timezone: %s\n" +
                        "Currency: %s\n" +
                        "Match Score: %s \n",
                symbol, name, type, region, marketOpen, marketClose, timezone, currency, matchScore
        );
    }

    public static String formattedList(List<StockMatch> listStockList){
        StringBuilder builder = new StringBuilder();
        int index = 1;

        for(StockMatch stock : listStockList) {
            builder.append("====================================\n");
            builder.append("Match #").append(index++).append("\n");
            builder.append("Symbol      : ").append(stock.getSymbol()).append("\n");
            builder.append("Name        : ").append(stock.getName()).append("\n");
            builder.append("Type        : ").append(stock.getType()).append("\n");
            builder.append("Region      : ").append(stock.getRegion()).append("\n");
            builder.append("Market Open : ").append(stock.getMarketOpen()).append("\n");
            builder.append("Market Close: ").append(stock.getMarketClose()).append("\n");
            builder.append("Timezone    : ").append(stock.getTimezone()).append("\n");
            builder.append("Currency    : ").append(stock.getCurrency()).append("\n");
            builder.append("Match Score : ").append(stock.getMatchScore()).append("\n");
        }
        return builder.toString();
    }
}
