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


    public CryptoInfo retrieveCryptoInfo (String symbol) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.okx.com/api/v5/market/index-tickers?instId="+symbol))
                .header("accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        CryptoInfo cryptoInfo = getCryptoInfo(response);

        return cryptoInfo;
    }

    private static CryptoInfo getCryptoInfo(HttpResponse<String> response) {
        JSONObject cryptoJson = new JSONObject(response);
        JSONArray data = cryptoJson.getJSONArray("data");
        JSONObject cryptoData = data.getJSONObject(0);

        CryptoInfo cryptoInfo = new CryptoInfo(
                cryptoData.getString("instId"),
                cryptoData.getString("idxPx"),
                cryptoData.getString("high24h"),
                cryptoData.getString("open24h"),
                cryptoData.getString("low24h"),
                cryptoData.getString("sodUtc0"),
                cryptoData.getString("sodUtc8")
        );
        return cryptoInfo;
    }
}
