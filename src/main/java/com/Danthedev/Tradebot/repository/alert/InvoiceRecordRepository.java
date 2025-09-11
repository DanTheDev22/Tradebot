package com.Danthedev.Tradebot.repository.alert;

import com.Danthedev.Tradebot.domain.model.InvoiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRecordRepository extends JpaRepository<InvoiceRecord, Long> {

    Optional<InvoiceRecord> findByChatId(long chatId);
}
