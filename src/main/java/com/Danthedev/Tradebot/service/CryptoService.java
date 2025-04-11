package com.Danthedev.Tradebot.service;

import com.Danthedev.Tradebot.model.CryptoAlert;
import com.Danthedev.Tradebot.model.CryptoData;
import com.Danthedev.Tradebot.repository.CryptoAlertRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

@Service
public class CryptoService {

    @Autowired
    private CryptoAlertRepository repository;

    @Autowired
    private static ObjectMapper objectMapper;

    @Autowired
    @Lazy
    private NotificationService notificationService;

    @Autowired
    private HttpClient httpClient;

    public CryptoData retrieveCryptoFullInfo (String symbol) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.okx.com/api/v5/market/index-tickers?instId="+symbol.toUpperCase()))
                .header("accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return getCryptoInfo(response);
    }

    public JSONObject retrieveCryptoPrice (String symbol) throws IOException, InterruptedException {
        symbol = formatSymbol(symbol);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://binance.com/api/v3/ticker/price?symbol="+symbol.toUpperCase()))
                .header("accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

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

    private static CryptoData getCryptoInfo(HttpResponse<String> response) throws JsonProcessingException {
        return objectMapper.readValue(response.body(), CryptoData.class);
    }

    public void createCryptoAlert(Long telegramUserId, String symbol, double targetPrice) {
        CryptoAlert alert = new CryptoAlert();
        alert.setTelegramUserId(telegramUserId);
        alert.setSymbol(symbol);
        alert.setTargetPrice(targetPrice);
        repository.save(alert);
    }

    @Scheduled(fixedRate = 60000)
    public void checkAlerts() throws IOException, InterruptedException {
        List<CryptoAlert> alerts = repository.findByNotifiedFalse();

        for (CryptoAlert alert : alerts) {
            JSONObject object = retrieveCryptoPrice(alert.getSymbol());
            double currentPrice = Double.parseDouble(object.getString("price"));

            if (currentPrice>= alert.getTargetPrice()) {
                notificationService.notifyUser(alert.getTelegramUserId(),alert.getSymbol() + " has reached " +
                        alert.getTargetPrice() + " ! Actual Price is " + currentPrice );
                alert.setNotified(true);
                repository.delete(alert);
            }
        }
    }

    public List<CryptoAlert> showAllMyAlerts(Long telegramUserId) {
        return repository.findByTelegramUserId(telegramUserId);
    }

    public void deleteMyAlert(String symbol) {
        Optional<CryptoAlert> foundAlert = repository.findBySymbol(symbol.toUpperCase());
        foundAlert.ifPresent(cryptoAlert -> repository.delete(cryptoAlert));
    }
}
