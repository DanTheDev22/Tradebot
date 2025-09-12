package com.Danthedev.tradebot.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class CryptoDataResponse {
    private List<CryptoData> data;
}
