package com.Danthedev.Tradebot.service;

import com.Danthedev.Tradebot.model.ListStock;
import com.Danthedev.Tradebot.model.StockInfo;
import org.json.JSONArray;
import org.json.JSONObject;
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

    private String apiKey = "H1X0GTYXQ9O9064A";

    public StockInfo retrieveStockInfo(String symbol) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol.toUpperCase() + "&apikey=" + apiKey))
                .header("accept","application/json")
                .method("GET",HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());

        return getStockInfo(response);
    }

    public StockInfo getStockInfo(HttpResponse<String> response) {
        JSONObject stockJson = new JSONObject(response.body());
        JSONObject stockResult = stockJson.getJSONObject("Global Quote");
        return new StockInfo(
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

    public List<ListStock> searchStockBySymbol(String symbol) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords="+ symbol +"&apikey=" + apiKey))
                .header("accept","application/json")
                .method("GET",HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());

        return parseStockSearchResponse(response);
    }

    private List<ListStock> parseStockSearchResponse(HttpResponse<String> response) {
        List<ListStock> stockList = new ArrayList<>();
        JSONObject responseJson = new JSONObject(response.body());
        JSONArray bestMatches = responseJson.getJSONArray("bestMatches");

        for (int i = 0; i < bestMatches.length(); i++) {
            JSONObject match = bestMatches.getJSONObject(i);
            ListStock stock = new ListStock(
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
