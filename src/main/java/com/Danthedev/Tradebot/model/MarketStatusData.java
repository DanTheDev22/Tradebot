package com.Danthedev.Tradebot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MarketStatusData {

    private String exchange;
    private String holiday;
    @JsonProperty(value = "isOpen")
    private boolean isOpen;
    private String session;
    private long t; //unix timestamp ignored (just for request purpose)
    private String timezone;

    @Override
    public String toString() {
        return String.format(
                "Market Status:\n" +
                        "Exchange: %s\n" +
                        "Holiday: %s\n" +
                        "Market Open: %b\n" +
                        "Session: %s\n" +
                        "Timezone: %s",
                exchange,
                (holiday == null ? "None" : holiday),
                isOpen,
                session,
                timezone
        );
    }
}
