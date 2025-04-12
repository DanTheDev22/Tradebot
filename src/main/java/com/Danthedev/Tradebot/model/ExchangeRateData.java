package com.Danthedev.Tradebot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExchangeRateData {

   private String fromCurrencyCode;
   private String fromCurrencyName;
   private String toCurrencyCode;
   private String toCurrencyName;
   private String exchangeRate;
   private String lastRefreshed;
   private String timeZone;
   private String bidPrice;
   private String askPrice;

   @Override
   public String toString() {
      return "ExchangeRateData {" +
              "\nFrom Currency: " + fromCurrencyName + " (" + fromCurrencyCode + ")" +
              "\nTo Currency: " + toCurrencyName + " (" + toCurrencyCode + ")" +
              "\nExchange Rate: " + exchangeRate +
              "\nLast Refreshed: " + lastRefreshed +
              "\nTime Zone: " + timeZone +
              "\nBid Price: " + bidPrice +
              "\nAsk Price: " + askPrice +
              "\n}";
   }
}
