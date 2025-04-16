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
      return String.format("""

                      From Currency   : %s (%s)
                      To Currency     : %s (%s)
                      Exchange Rate   : %s
                      Last Refreshed  : %s
                      Time Zone       : %s
                      Bid Price       : %s
                      Ask Price       : %s
                      """,
              fromCurrencyName, fromCurrencyCode,
              toCurrencyName, toCurrencyCode,
              exchangeRate, lastRefreshed,
              timeZone, bidPrice, askPrice);
   }
}
