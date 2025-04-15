package com.Danthedev.Tradebot.service;

import com.Danthedev.Tradebot.dto.MarketStatusData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


@Service
public class MarketStatusService {

    @Value("${FinnHub.API.token}")
    private String api_key;

    private static final String API_URL = "https://finnhub.io/api/v1/stock/market-status?exchange=US&token=";

    public String getMarketStatus() throws IOException {
        URL url = new URL(API_URL+api_key);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder result = new StringBuilder();
            String nextLine;
            while ((nextLine = in.readLine()) != null) {
                result.append(nextLine);
            }

            ObjectMapper mapper = new ObjectMapper();
            return String.valueOf(mapper.readValue(result.toString(), MarketStatusData.class));
        }
    }
}
