package com.Danthedev.Tradebot.service;

import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class CryptoPayClient {
    private static final String API_URL = "https://testnet-pay.crypt.bot/api/";

    private final String token;

    private final HttpClient client;

    @Autowired
    public CryptoPayClient(@Value("${CryptoBot.API.token}") String token, HttpClient client) {
        this.token = token;
        this.client = client;
    }


    public String createInvoice (String currency, double amount, String description) throws IOException, InterruptedException {
        String body = String.format(
                "currency=%s&amount=%f&description=%s",
                currency, amount, description
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "createInvoice"))
                .header("Crypto-Pay-API-Token",token)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        return extractPayUrl(response);
    }

    private String extractPayUrl(HttpResponse<String> response) {
        JSONObject fullResponse = new JSONObject(response.body());
        JSONObject result = fullResponse.getJSONObject("result");
        return result.getString("pay_url");
    }
}
