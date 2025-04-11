package com.Danthedev.Tradebot.repository;

import com.Danthedev.Tradebot.model.CryptoAlert;
import com.Danthedev.Tradebot.model.CryptoData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CryptoAlertRepository extends JpaRepository<CryptoAlert, Long> {

    List<CryptoAlert> findByNotifiedFalse();

    List<CryptoAlert> findByTelegramUserId(long telegramUserId);

    Optional<CryptoAlert> findBySymbol(String symbol);
}
