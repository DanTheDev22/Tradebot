package com.Danthedev.Tradebot.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

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
                """
                        Symbol      : %s
                        Name        : %s
                        Type        : %s
                        Region      : %s
                        Market Open : %s
                        Market Close: %s
                        Timezone    : %s
                        Currency    : %s
                        Match Score : %s
                        """,
                symbol, name, type, region, marketOpen, marketClose, timezone, currency, matchScore
        );
    }

    public static String formatStockList(List<StockMatch> stockList) {
        return stockList.stream()
                .map(stock -> String.format(
                        """
                                ====================================
                                Match #%d
                                Symbol      : %s
                                Name        : %s
                                Type        : %s
                                Region      : %s
                                Market Open : %s
                                Market Close: %s
                                Timezone    : %s
                                Currency    : %s
                                Match Score : %s
                                """,
                        stockList.indexOf(stock) + 1,
                        stock.getSymbol(),
                        stock.getName(),
                        stock.getType(),
                        stock.getRegion(),
                        stock.getMarketOpen(),
                        stock.getMarketClose(),
                        stock.getTimezone(),
                        stock.getCurrency(),
                        stock.getMatchScore()
                ))
                .collect(Collectors.joining("\n"));
    }
}

