package com.Danthedev.Tradebot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CryptoInfo {

    private String instId;
    private String idxPx;
    private String high24h;
    private String open24h;
    private String low24h;
    private String sodUtc0;
    private String sodUtc8;


    @Override
    public String toString() {
        return "Symbol: " + instId + "\n" +
                "Index Price: " + idxPx + "\n" +
                "High (24h): " + high24h + "\n" +
                "Open (24h): " + open24h + "\n" +
                "Low (24h): " + low24h + "\n" +
                "Start of Day (UTC 0): " + sodUtc0 + "\n" +
                "Start of Day (UTC 8): " + sodUtc8;
    }
}
