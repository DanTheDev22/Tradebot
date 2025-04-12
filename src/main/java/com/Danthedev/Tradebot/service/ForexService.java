package com.Danthedev.Tradebot.service;

import com.Danthedev.Tradebot.model.ExchangeRateData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static ObjectMapper objectMapper;

    @Autowired
    private HttpClient httpClient;

    //supports a pair of digital currency (e.g., Bitcoin) and physical currency (e.g., USD)
    public ExchangeRateData retrieveCurrencyExchangeRate (String fromCurrency, String toCurrency) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency="
                        + fromCurrency.toUpperCase() + " &to_currency=" + toCurrency.toUpperCase() + " &apikey=" + apiKey))
                .method("GET",HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

        return getExchangeRateData(response);
    }

    private static ExchangeRateData getExchangeRateData(HttpResponse<String> response) throws JsonProcessingException {
        return objectMapper.readValue(response.body(), ExchangeRateData.class);
    }
}
