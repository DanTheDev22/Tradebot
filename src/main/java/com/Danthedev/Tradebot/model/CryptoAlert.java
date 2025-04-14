package com.Danthedev.Tradebot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class CryptoAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long id;

    @Column(name = "telegram_user_id")
    private Long telegramUserId;

    private String symbol;

    @Column(name = "target_price")
    private Double targetPrice;

    private boolean notified = false;

    @Override
    public String toString() {
        return String.format(
                """
                        📈 Symbol: %s
                        🎯 Target Price: %.2f
                        🔔 Notified: %s
                        """,
                symbol,
                targetPrice,
                notified ? "✅ Yes" : "❌ No"
        );
    }

    public static String formatCryptoAlertList(List<CryptoAlert> cryptoAlertList) {
        if (cryptoAlertList.isEmpty()) {
            return "🔕 You have no active alerts.";
        }

        return cryptoAlertList.stream()
                .map(alert -> String.format(
                        "Alert #%d\n%s\n--------------------------",
                        cryptoAlertList.indexOf(alert) + 1,
                        alert.toString()))
                .collect(Collectors.joining("\n\n"));
    }
}
