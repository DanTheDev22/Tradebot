package com.Danthedev.Tradebot.service;

import com.Danthedev.Tradebot.dto.CryptoDataResponse;
import com.Danthedev.Tradebot.model.CryptoAlert;
import com.Danthedev.Tradebot.dto.CryptoData;
import com.Danthedev.Tradebot.repository.CryptoAlertRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class CryptoService {

    @Autowired
    private CryptoAlertRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Lazy
    private NotificationService notificationService;

    @Autowired
    private HttpClient httpClient;

    public CryptoData retrieveCryptoFullInfo(String symbol) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.okx.com/api/v5/market/index-tickers?instId=" + symbol.toUpperCase()))
                .header("accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("OKX API responded with status " + response.statusCode());
        }
        return getCryptoInfo(response);
    }


    public JSONObject retrieveCryptoPrice(String symbol) throws IOException, InterruptedException {
        symbol = formatSymbol(symbol);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://binance.com/api/v3/ticker/price?symbol=" + symbol.toUpperCase()))
                .header("accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return new JSONObject(response.body());
        } else {
            log.warn("Failed to retrieve price for '{}'. Status code: {}", symbol, response.statusCode());
            throw new IOException("❌ Could not retrieve price. Please try again later.");
        }
    }

    private String formatSymbol(String symbol) {
        return symbol.replace("-", "");
    }

    private CryptoData getCryptoInfo(HttpResponse<String> response) throws JsonProcessingException {
        CryptoDataResponse wrapper = objectMapper.readValue(response.body(), CryptoDataResponse.class);

        if (wrapper != null && !wrapper.getData().isEmpty()) {
            return wrapper.getData().get(0);
        } else {
            return null;
        }
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

        if (alerts.isEmpty()) {
            log.info("No active alerts to check.");
            return;
        }

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

    public boolean deleteMyAlert(Long telegramUserId, String symbol) {
        Optional<CryptoAlert> foundAlert = repository.findByTelegramUserIdAndSymbol(telegramUserId, symbol.toUpperCase());

        if (foundAlert.isPresent()) {
            repository.delete(foundAlert.get());
            log.info("✅ Alert for symbol '{}' deleted for user {}", symbol.toUpperCase(), telegramUserId);
            return true;
        } else {
            log.warn("⚠️ No alert found for symbol '{}' for user {}", symbol.toUpperCase(), telegramUserId);
            return false;
        }
    }

    public JSONObject searchBySymbolOrByName(String symbol) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("https://data-api.coindesk.com/asset/v1/search?limit=1&search_string=" + symbol))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject result = new JSONObject(response.body());

            if (result.isEmpty() || !result.has("data") || result.getJSONArray("data").isEmpty()) {
                throw new IOException("🔍 No results found for: '" + symbol + "'. Try a different symbol or name.");
            }

            return result;
        } else {
            log.warn("Search failed for '{}'. Status code: {}", symbol, response.statusCode());
            throw new IOException("❌ Could not search for symbol. Please try again later.");
        }
    }
}
