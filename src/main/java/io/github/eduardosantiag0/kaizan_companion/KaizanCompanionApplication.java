package io.github.eduardosantiag0.kaizan_companion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@SpringBootApplication
public class KaizanCompanionApplication {

	public static void main(String[] args) throws TelegramApiException, InstantiationException, IllegalAccessException {
//        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
//        AnnotationConfigApplicationContext context
//                = new AnnotationConfigApplicationContext();
//        botsApi.registerBot(context.getBean("${BOT_NAME}"),
//                TelegramBotService.class.newInstance());
		SpringApplication.run(KaizanCompanionApplication.class, args);
	}

}
