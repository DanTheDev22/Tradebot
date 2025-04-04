package com.Danthedev.Tradebot.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class CryptoService {


    public String retrieveCryptoInfo (String symbol) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.coingecko.com/api/v3/simple/price?ids=" + symbol + "&vs_currencies=usd&include_market_cap=true&include_24hr_vol=true&include_24hr_change=true&include_last_updated_at=true"))
                .header("accept", "application/json")
                .header("x-cg-demo-api-key", "CG-yHMjv8fKV1CUVDxnMDXP7WUV")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
