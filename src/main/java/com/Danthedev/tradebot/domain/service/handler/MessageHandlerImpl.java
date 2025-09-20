package com.Danthedev.tradebot.domain.service.handler;

import com.Danthedev.tradebot.domain.service.AccessManagerService;
import com.Danthedev.tradebot.domain.service.UserStateService;
import com.Danthedev.tradebot.domain.service.handler.crypto.CryptoMessageHandler;
import com.Danthedev.tradebot.telegram.BotSender;
import com.Danthedev.tradebot.command.CommandHandlerImpl;
import com.Danthedev.tradebot.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;


@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandlerImpl {

    private final BotSender bot;
    private final CommandHandlerImpl commandHandler;
    private final UserRepository userRepository;
    private final AccessManagerService accessManager;
    private final UserSessionRouter sessionRouter;
    private final UserStateService userStateService;
    private final CryptoMessageHandler cryptoMessageHandler;

    public void routeUserMessage(Update update) {
        long chatId = update.getMessage().getChatId();
        String username = update.getMessage().getFrom().getFirstName();
        String messageText = update.getMessage().getText();

        handleNewUser(chatId, username);

        if (!accessManager.ensureAccess(chatId)) {
            return;
        }

        if (messageText.startsWith("/")) {
            userStateService.clearSession(chatId);
            try {
                commandHandler.handleCommand(messageText, chatId, username);
            } catch (Exception e) {
                bot.sendText(chatId, "❌ Something went wrong while executing that command.", true);
                log.error("Command '{}' failed for user {}: {}", messageText, chatId, e.getMessage(), e);
            }
            return;
        }

        String currentState = userStateService.getState(chatId);
        if (currentState != null) {
            log.debug("Routing message '{}' from {} based on state {}", messageText, chatId, currentState);
            boolean handled = sessionRouter.route(chatId, messageText);
            if (!handled) {
                bot.sendText(chatId, "⚠️ Something went wrong while processing your request.", true);
                log.warn("State {} was not handled for user {}", currentState, chatId);
            }
            return;
        }

        bot.sendText(chatId, "🤖 I'm not sure what you're trying to do. Use /start to see available commands.", true);
        log.info("Unhandled message: '{}' from user {}", messageText, chatId);
    }

    private void handleNewUser(long chatId, String username) {
        if (chatId == 0) return;
        userRepository.insertIfNotExists(chatId, username, LocalDateTime.now(), false);
    }

    public void checkIfDeleted(boolean isDeleted, long chatId) {
        if (isDeleted) {
            bot.sendText(chatId, "✅ Alert successfully deleted.", false);
        } else {
            bot.sendText(chatId, "⚠️ Alert could not be deleted. It may have been already removed.", true);
        }
    }

    public boolean deleteAlertByIdAbstract(Long chatId, Long alertId) {
       return cryptoMessageHandler.deleteAlertById(chatId,alertId);
    }
}

