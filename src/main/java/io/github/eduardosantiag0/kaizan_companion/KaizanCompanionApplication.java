package io.github.eduardosantiag0.kaizan_companion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@SpringBootApplication
@EnableJpaAuditing
@EnableRetry
@EnableScheduling
public class KaizanCompanionApplication {

	public static void main(String[] args) {
		SpringApplication.run(KaizanCompanionApplication.class, args);
	}

}
