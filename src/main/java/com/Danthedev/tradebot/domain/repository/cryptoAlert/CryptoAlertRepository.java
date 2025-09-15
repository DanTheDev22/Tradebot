package com.Danthedev.tradebot.domain.repository.cryptoAlert;

import com.Danthedev.tradebot.domain.model.CryptoAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CryptoAlertRepository extends JpaRepository<CryptoAlert, Long> {

    List<CryptoAlert> findByNotifiedFalse();

    List<CryptoAlert> findByTelegramUserId(long telegramUserId);

    Optional<CryptoAlert> findByTelegramUserIdAndSymbol(long telegramUserId, String symbol);

    List<CryptoAlert> findAllByTelegramUserIdAndSymbol(Long telegramUserId, String symbol);
}
