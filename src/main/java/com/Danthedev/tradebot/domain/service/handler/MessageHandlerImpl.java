package com.Danthedev.tradebot.domain.service.handler;

import com.Danthedev.tradebot.domain.service.AccessManagerService;
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

    public void routeUserMessage(Update update) {
        long chatId = update.getMessage().getChatId();
        String username = update.getMessage().getFrom().getFirstName();
        String messageText = update.getMessage().getText();

        handleNewUser(chatId, username);

        if (!accessManager.ensureAccess(chatId)) {
            return;
        }

        if (messageText.startsWith("/")) {
            try {
                commandHandler.handleCommand(messageText, chatId, username);
            } catch (Exception e) {
                bot.sendText(chatId, "❌ Something went wrong while executing that command.", true);
                log.error("Command '{}' failed for user {}: {}", messageText, chatId, e.getMessage(), e);
            }
            return;
        }

        boolean handled = sessionRouter.route(chatId, messageText);
        if (!handled) {
            bot.sendText(chatId, "🤖 I'm not sure what you're trying to do. Use /start to see available commands.", true);
            log.info("Unhandled message: '{}' from user {}", messageText, chatId);
        }
    }

    private void handleNewUser(long chatId, String username) {
        if (chatId == 0) return;
        userRepository.insertIfNotExists(chatId, username, LocalDateTime.now());
    }
}

