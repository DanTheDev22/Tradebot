package com.Danthedev.Tradebot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    public static String formattedListCrypto(List<CryptoAlert> cryptoAlertList){
        if (cryptoAlertList.isEmpty()) {
            return "🔕 You have no active alerts.";
        }

        StringBuilder sb = new StringBuilder("📋 Your Alerts:\n\n");
        int index = 1;

        for(CryptoAlert alert : cryptoAlertList) {
            sb.append("Alert #").append(index++).append("\n");
            sb.append(alert.toString()).append("\n");
            sb.append("--------------------------\n");
        }
        return sb.toString();
    }
}
