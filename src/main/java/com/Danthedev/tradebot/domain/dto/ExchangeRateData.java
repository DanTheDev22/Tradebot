package com.Danthedev.tradebot.domain.dto;


public record ExchangeRateData(String fromCurrencyCode, String fromCurrencyName, String toCurrencyCode,
                               String toCurrencyName, String exchangeRate, String lastRefreshed, String timeZone,
                               String bidPrice, String askPrice) {

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
