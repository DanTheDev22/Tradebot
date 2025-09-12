package com.Danthedev.tradebot.domain.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MarketStatusData(String exchange, String holiday, @JsonProperty(value = "isOpen") boolean isOpen,
                               String session, @JsonIgnore long timestamp, String timezone) {

    @JsonCreator
    public MarketStatusData(
            @JsonProperty("exchange") String exchange,
            @JsonProperty("holiday") String holiday,
            @JsonProperty("isOpen") boolean isOpen,
            @JsonProperty("session") String session,
            @JsonProperty("t") long timestamp,
            @JsonProperty("timezone") String timezone
    ) {
        this.exchange = exchange;
        this.holiday = holiday;
        this.isOpen = isOpen;
        this.session = session;
        this.timestamp = timestamp;
        this.timezone = timezone;
    }

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
