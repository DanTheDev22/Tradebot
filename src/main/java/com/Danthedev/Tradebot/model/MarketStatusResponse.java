package com.Danthedev.Tradebot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MarketStatusResponse {

    private String exchange;
    private String holiday;
    @JsonProperty(value = "isOpen")
    private boolean isOpen;
    private String session;
    private long t;
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
