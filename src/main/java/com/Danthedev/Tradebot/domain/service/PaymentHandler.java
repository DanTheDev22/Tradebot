package com.Danthedev.Tradebot.domain.service;

import com.Danthedev.Tradebot.telegram.BotSender;
import com.Danthedev.Tradebot.common.Status;
import com.Danthedev.Tradebot.domain.model.InvoiceRecord;
import com.Danthedev.Tradebot.domain.model.User;
import com.Danthedev.Tradebot.repository.alert.InvoiceRecordRepository;
import com.Danthedev.Tradebot.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentHandler {

    private final UserRepository userRepository;
    private final InvoiceRecordRepository invoiceRep;
    private final BotSender bot;

    private final Map<Long, InvoiceRecord> pendingInvoices = new HashMap<>();

    boolean checkForAccess(Long chatId) {
        User user = userRepository.findByUserId(chatId);
        return user.getHasAccess();
    }

    private void handlePreCheckout(PreCheckoutQuery preCheckoutQuery, long chatId) {
        AnswerPreCheckoutQuery answer = AnswerPreCheckoutQuery.builder()
                .preCheckoutQueryId(preCheckoutQuery.getId())
                .ok(true)
                .errorMessage(null)
                .build();


        bot.answerPreCheckoutQuery(answer);
        log.info("PreCheckoutQuery {} answered with ok={true}", preCheckoutQuery.getId());

        InvoiceRecord invoice = new InvoiceRecord();
        invoice.setChatId(chatId);
        invoice.setPayload(preCheckoutQuery.getInvoicePayload());
        invoice.setProvider("RunPay");
        invoice.setStatus(Status.PENDING);
        invoice.setCreatedAt(LocalDateTime.now());

        pendingInvoices.put(chatId, invoice);

        checkPaymentButton(chatId);

    }

    private void checkPaymentButton(long chatId) {
        InlineKeyboardButton checkPay = new InlineKeyboardButton();
        checkPay.setText("Check Payment");
        checkPay.setCallbackData("CHECK_PAYMENT");

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(checkPay)))
                .build();

        bot.sendText(chatId, "Click below to verify your payment:", true, markup);
    }

    public void handleSuccessfulPayment(SuccessfulPayment payment, long chatId) {
        log.info("Payment received: {}", payment);

        InvoiceRecord invoice = pendingInvoices.get(chatId);

        if (invoice != null) {
            invoice.setStatus(Status.APPROVED);
            invoiceRep.save(invoice);

            pendingInvoices.remove(chatId);
        }

        User user = userRepository.findByUserId(chatId);
        if (user != null) {
            user.setHasAccess(true);
            userRepository.save(user);
        }

        bot.sendText(chatId, "✅ Payment successful! You now have premium access.", true);
    }


    public void handlePaymentUpdate(Update update, long chatId) {
        if (update.hasPreCheckoutQuery()) {
            handlePreCheckout(update.getPreCheckoutQuery(), chatId);
        } else if (update.hasMessage() && update.getMessage().hasSuccessfulPayment()) {
            handleSuccessfulPayment(update.getMessage().getSuccessfulPayment(), chatId);
        } else {
            log.warn("Payment failed or timed out for user {}", chatId);
            bot.sendText(chatId, "❌ Payment failed or timed out. Please try again.", true);
        }
    }

    public void checkPayment(long chatId) {
        Optional<InvoiceRecord> invoice = invoiceRep.findByChatId(chatId);

        if (invoice.isPresent() && invoice.get().getStatus() == Status.APPROVED) {
            bot.sendText(chatId, "✅ Payment successful! You now have premium access.", true);
        } else {
            bot.sendText(chatId, "❌ Payment not found or not approved.", true);
        }
    }
}
