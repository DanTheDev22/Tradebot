package com.Danthedev.Tradebot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Data
@AllArgsConstructor
public class CryptoData {

    private String instId;
    private String idxPx;
    private String high24h;
    private String open24h;
    private String low24h;
    private String sodUtc0;
    private String sodUtc8;



    @Override
    public String toString() {
        BigDecimal indexPrice = Optional.ofNullable(idxPx)
                .map(BigDecimal::new)
                .orElse(BigDecimal.ZERO);

        BigDecimal openPrice = Optional.ofNullable(open24h)
                .map(BigDecimal::new)
                .orElse(BigDecimal.ZERO);

        BigDecimal change = indexPrice.subtract(openPrice);
        BigDecimal changePercent = openPrice.compareTo(BigDecimal.ZERO) != 0 ?
                change.divide(openPrice,6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;


        return "Symbol: " + instId + "\n" +
                "Index Price: " + idxPx + "$\n" +
                String.format("Price Change: %.2f$\n", change) +
                String.format("Price Change %%: %.2f%%\n", changePercent) +
                "High (24h): " + high24h + "$\n" +
                "Open (24h): " + open24h + "$\n" +
                "Low (24h): " + low24h + "$\n" +
                "Start of Day (UTC 0): " + sodUtc0 + "$\n" +
                "Start of Day (UTC 8): " + sodUtc8 + "$";
    }

}
