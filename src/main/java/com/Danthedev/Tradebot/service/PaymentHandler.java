package com.Danthedev.Tradebot.service;

import com.Danthedev.Tradebot.BotSenderImpl;
import com.Danthedev.Tradebot.model.User;
import com.Danthedev.Tradebot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentHandler {

    private final UserRepository userRepository;
    private final BotSenderImpl bot;

    boolean checkForAccess(Long chatId) {
        User user = userRepository.findByUserId(chatId);
        return user.getHasAccess();
    }

    private void handlePreCheckout(PreCheckoutQuery preCheckoutQuery) {
        try {

            AnswerPreCheckoutQuery answer = AnswerPreCheckoutQuery.builder()
                    .preCheckoutQueryId(preCheckoutQuery.getId())
                    .ok(true)
                    .errorMessage(null)
                    .build();

            bot.execute(answer);
            log.info("PreCheckoutQuery {} answered with ok={true}", preCheckoutQuery.getId());

        } catch (TelegramApiException e) {
            log.error("Failed to answer PreCheckoutQuery: {}", e.getMessage(), e);
        }
    }

    private void handleSuccessfulPayment(SuccessfulPayment payment) {
        log.info("Payment received: {}", payment);

        long userId = Long.parseLong(payment.getTelegramPaymentChargeId());

        User user = userRepository.findByUserId(userId);
        if (user != null) {
            user.setHasAccess(true);
            userRepository.save(user);
        }

        bot.sendMessage(userId, "✅ Payment successful! You now have premium access.", true);
    }

    public void handlePaymentUpdate(Update update) {
        if (update.hasPreCheckoutQuery()) {
            handlePreCheckout(update.getPreCheckoutQuery());
        } else if (update.hasMessage() && update.getMessage().hasSuccessfulPayment()) {
            handleSuccessfulPayment(update.getMessage().getSuccessfulPayment());
        }
    }
}
