package com.Danthedev.Tradebot.service;

import com.Danthedev.Tradebot.model.CryptoInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class CryptoService {

    public CryptoInfo retrieveCryptoFullInfo (String symbol) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.okx.com/api/v5/market/index-tickers?instId="+symbol.toUpperCase()))
                .header("accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        return getCryptoInfo(response);
    }

    public JSONObject retrieveCryptoPrice (String symbol) throws IOException, InterruptedException {
        symbol = formatSymbol(symbol);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://binance.com/api/v3/ticker/price?symbol="+symbol.toUpperCase()))
                .header("accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            String result = response.body();
            return new JSONObject(result);
        } else {
            System.out.println("Error: Received status code " + response.statusCode());
            throw new IOException("Failed to retrieve price. Status code: " + response.statusCode());
        }
    }

    private String formatSymbol(String symbol) {
        return symbol.replace("-", "");
    }

    private static CryptoInfo getCryptoInfo(HttpResponse<String> response) {
        JSONObject cryptoJson = new JSONObject(response.body());
        JSONArray data = cryptoJson.getJSONArray("data");
        JSONObject cryptoData = data.getJSONObject(0);

        return new CryptoInfo(
                cryptoData.getString("instId"),
                cryptoData.getString("idxPx"),
                cryptoData.getString("high24h"),
                cryptoData.getString("open24h"),
                cryptoData.getString("low24h"),
                cryptoData.getString("sodUtc0"),
                cryptoData.getString("sodUtc8")
        );
    }
}
