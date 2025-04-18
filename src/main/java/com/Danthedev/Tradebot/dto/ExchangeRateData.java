package com.Danthedev.Tradebot.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ExchangeRateData {

   private final String fromCurrencyCode;
   private final String fromCurrencyName;
   private final String toCurrencyCode;
   private final String toCurrencyName;
   private final String exchangeRate;
   private final String lastRefreshed;
   private final String timeZone;
   private final String bidPrice;
   private final String askPrice;

   @Override
   public String toString() {
      double bid = Double.parseDouble(bidPrice);
      double ask = Double.parseDouble(askPrice);
      double spread = ask - bid;

      return String.format("""
                      %s (%s) → %s (%s)
                      Exchange Rate: %s
                      Bid: %.8f | Ask: %.8f | Spread: %.8f
                      Last Refreshed: %s
                      Time Zone: %s
                      """,
              fromCurrencyName, fromCurrencyCode,
              toCurrencyName, toCurrencyCode,
              exchangeRate, bid, ask, spread,
              lastRefreshed, timeZone);
   }
}
