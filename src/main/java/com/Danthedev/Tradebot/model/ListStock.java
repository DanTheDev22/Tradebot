package com.Danthedev.Tradebot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ListStock {

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
}
