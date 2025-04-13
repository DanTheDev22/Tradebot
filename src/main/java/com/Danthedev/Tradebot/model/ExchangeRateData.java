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
      return "\n" +
              "From Currency   : " + fromCurrencyName + " (" + fromCurrencyCode + ")\n" +
              "To Currency     : " + toCurrencyName + " (" + toCurrencyCode + ")\n" +
              "Exchange Rate   : " + exchangeRate + "\n" +
              "Last Refreshed  : " + lastRefreshed + "\n" +
              "Time Zone       : " + timeZone + "\n" +
              "Bid Price       : " + bidPrice + "\n" +
              "Ask Price       : " + askPrice + "\n";
   }
}
