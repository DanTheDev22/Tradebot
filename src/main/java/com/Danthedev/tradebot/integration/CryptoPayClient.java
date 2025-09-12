package com.Danthedev.tradebot.integration;

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

    public String createInvoice (Invoice invoice) throws IOException, InterruptedException {
        String body = invoice.toJson();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "createInvoice"))
                .header("Crypto-Pay-API-Token",token)
                .header("Content-Type", "application/json")
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


        public record Invoice(String currencyType, String asset, String fiat, double amount, String description,
                              String payload, int expiresIn) {


            public String toJson() {
                String safeDescription = description.replace("\"", "\\\"");
                String safePayload = payload != null ? payload.replace("\"", "\\\"") : "";
                StringBuilder json = new StringBuilder("{");
                json.append("\"currency_type\":\"").append(currencyType).append("\",");
                if ("crypto".equals(currencyType)) json.append("\"asset\":\"").append(asset).append("\",");
                if ("fiat".equals(currencyType)) json.append("\"fiat\":\"").append(fiat).append("\",");
                json.append("\"amount\":").append(amount).append(",");
                json.append("\"description\":\"").append(safeDescription).append("\",");
                json.append("\"payload\":\"").append(safePayload).append("\",");
                json.append("\"expires_in\":").append(expiresIn);
                json.append("}");
                return json.toString();
            }
        }
}
