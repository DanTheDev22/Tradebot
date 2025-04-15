package com.Danthedev.Tradebot.service;

import com.Danthedev.Tradebot.dto.StockData;
import com.Danthedev.Tradebot.dto.StockMatch;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockService {

    @Value("${AlphaVantage.API.token}")
    private String apiKey;

    public StockData retrieveStockInfo(String symbol) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol.toUpperCase() + "&apikey=" + apiKey))
                .header("accept","application/json")
                .method("GET",HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());
        return getStockInfo(response);
    }

    public StockData getStockInfo(HttpResponse<String> response) {
        JSONObject stockJson = new JSONObject(response.body());
        JSONObject stockResult = stockJson.getJSONObject("Global Quote");
        return new StockData(
              stockResult.getString("01. symbol"),
              stockResult.getString("02. open"),
              stockResult.getString("03. high"),
              stockResult.getString("04. low"),
              stockResult.getString("05. price"),
              stockResult.getString("06. volume"),
              stockResult.getString("07. latest trading day"),
              stockResult.getString("08. previous close"),
              stockResult.getString("09. change"),
              stockResult.getString("10. change percent")
        );
    }

    public List<StockMatch> searchStockBySymbol(String symbol) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords="+ symbol +"&apikey=" + apiKey))
                .header("accept","application/json")
                .method("GET",HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());
        return parseStockSearchResponse(response);
    }

    private List<StockMatch> parseStockSearchResponse(HttpResponse<String> response) {
        List<StockMatch> stockList = new ArrayList<>();
        JSONObject responseJson = new JSONObject(response.body());
        JSONArray bestMatches = responseJson.getJSONArray("bestMatches");

        for (int i = 0; i < bestMatches.length(); i++) {
            JSONObject match = bestMatches.getJSONObject(i);
            StockMatch stock = new StockMatch(
                    match.getString("1. symbol"),
                    match.getString("2. name"),
                    match.getString("3. type"),
                    match.getString("4. region"),
                    match.getString("5. marketOpen"),
                    match.getString("6. marketClose"),
                    match.getString("7. timezone"),
                    match.getString("8. currency"),
                    match.getString("9. matchScore")
            );
            stockList.add(stock);
        }
        return stockList;
    }
}
