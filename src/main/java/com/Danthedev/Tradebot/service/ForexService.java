package com.Danthedev.Tradebot.service;

import com.Danthedev.Tradebot.model.ExchangeRateData;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class ForexService {

    private String apiKey = "H1X0GTYXQ9O9064A";


    @Autowired
    private HttpClient httpClient;

    //supports a pair of digital currency (e.g., Bitcoin) and physical currency (e.g., USD)
    public ExchangeRateData retrieveCurrencyExchangeRate (String fromCurrency, String toCurrency) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency="
                        + fromCurrency.toUpperCase() + "&to_currency=" + toCurrency.toUpperCase() + "&apikey=" + apiKey))
                .method("GET",HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        return getExchangeRateData(response);
    }

    private ExchangeRateData getExchangeRateData(HttpResponse<String> response) throws JsonProcessingException {
        JSONObject object = new JSONObject(response.body());
        JSONObject exchangeData = object.getJSONObject("Realtime Currency Exchange Rate");

        return new ExchangeRateData(
                exchangeData.getString("1. From_Currency Code"),
                exchangeData.getString("2. From_Currency Name"),
                exchangeData.getString("3. To_Currency Code"),
                exchangeData.getString("4. To_Currency Name"),
                exchangeData.getString("5. Exchange Rate"),
                exchangeData.getString("6. Last Refreshed"),
                exchangeData.getString("7. Time Zone"),
                exchangeData.getString("8. Bid Price"),
                exchangeData.getString("9. Ask Price")
        );
    }
}
