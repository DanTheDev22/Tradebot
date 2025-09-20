package com.Danthedev.tradebot.domain.service;

import com.Danthedev.tradebot.telegram.BotSender;
import com.Danthedev.tradebot.integration.CryptoPayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessManagerService {

    private final PaymentHandler paymentHandler;
    private final CryptoPayClient cryptoPayClient;
    private final BotSender bot;

    @Value("${RunPay.API.token}")
    private String RunPayToken;

    public boolean ensureAccess(Long chatId) {
        if (paymentHandler.checkForAccess(chatId)) return true;

        bot.sendError(chatId, "⚠️ You don't have access. Please purchase a subscription.");

        try {
            sendCryptoPayOptions(chatId);
            sendRunPayInvoice(chatId);
        } catch (Exception e) {
            bot.sendError(chatId, "Something went wrong while preparing payment options.");
            return false;
        }

        return false;
    }

    private void sendCryptoPayOptions(Long chatId) throws IOException, InterruptedException {
        CryptoPayClient.Invoice invoiceCrypto = new CryptoPayClient.Invoice(
                "crypto",
                "USDT",
                null,
                2.0,
                "TON Payment",
                "Regular",
                3600
        );
        String invoiceLink1 = cryptoPayClient.createInvoice(invoiceCrypto);

        CryptoPayClient.Invoice invoiceFiat = new CryptoPayClient.Invoice(
                "fiat",
                null,
                "USD",
                10.0,
                "USD Payment",
                "Regular",
                3600
        );
        String invoiceLink2 = cryptoPayClient.createInvoice(invoiceFiat);

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(
                        InlineKeyboardButton.builder().text("CryptoApi Pay").url(invoiceLink1).build(),
                        InlineKeyboardButton.builder().text("FiatApi Pay").url(invoiceLink2).build()
                )))
                .build();

        bot.sendText(chatId, "Choose your CryptoPay method:", true, markup);
// For accepting payment from cryptoapi its needed a domain and pre-checkout query + webhooks enabled
    }

    private void sendRunPayInvoice(Long chatId) {
        LabeledPrice price = new LabeledPrice("Subscription", 6000);
        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(
                        InlineKeyboardButton.builder().text("RunPay Provider").pay(true).build()
                )))
                .build();

        SendInvoice invoice = SendInvoice.builder()
                .chatId(chatId)
                .title("RunPay Provider")
                .description("One time purchase")
                .payload("purchase number 001")
                .providerToken(RunPayToken)
                .currency("MDL")
                .needEmail(true)
                .prices(List.of(price))
                .needName(true)
                .needPhoneNumber(true)
                .needShippingAddress(false)
                .replyMarkup(markup)
                .startParameter("premium123")
                .build();

        bot.sendText(chatId, "Or pay by your local provider:", true);
        bot.sendInvoice(invoice);
    }
}

