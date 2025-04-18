package com.Danthedev.Tradebot;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@EnableAspectJAutoProxy
@SpringBootApplication
@EnableScheduling
public class TradebotApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

		dotenv.entries().forEach(entry -> {
					if (System.getenv(entry.getKey()) == null && System.getProperty(entry.getKey()) == null) {
						System.setProperty(entry.getKey(), entry.getValue());
					}
				});

		SpringApplication.run(TradebotApplication.class, args);
	}

	@Bean
	public TelegramBotsApi telegramBotsApi(TelegramBot telegramBot) throws TelegramApiException {
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
		telegramBotsApi.registerBot(telegramBot);
		return telegramBotsApi;
	}

}
