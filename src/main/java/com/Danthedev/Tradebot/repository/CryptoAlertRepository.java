package com.Danthedev.Tradebot.repository;

import com.Danthedev.Tradebot.model.CryptoAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CryptoAlertRepository extends JpaRepository<CryptoAlert, Long> {

    List<CryptoAlert> findByNotifiedFalse();
}
