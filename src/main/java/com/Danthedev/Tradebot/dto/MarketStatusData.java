package com.Danthedev.Tradebot.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MarketStatusData {

    private final String exchange;
    private final String holiday;

    @JsonProperty(value = "isOpen")
    private final boolean isOpen;

    private final String session;

    @JsonIgnore
    private final long timestamp; //unix timestamp ignored (just for request purpose)

    private final String timezone;

    @Override
    public String toString() {
        return String.format(
                """
                        Market Status:
                        Exchange: %s
                        Holiday: %s
                        Market Open: %b
                        Session: %s
                        Timezone: %s""",
                exchange,
                (holiday == null ? "None" : holiday),
                isOpen,
                session,
                timezone
        );
    }
}
