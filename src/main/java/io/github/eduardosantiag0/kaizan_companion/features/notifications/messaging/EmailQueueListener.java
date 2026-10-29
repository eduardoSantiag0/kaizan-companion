package io.github.eduardosantiag0.kaizan_companion.features.notifications.messaging;

import io.github.eduardosantiag0.kaizan_companion.infra.providers.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailQueueListener {

    private final EmailService emailService;

    public EmailQueueListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(
            queues = "${rabbitmq.queue.external.email}"
    )
    public void listen(
            @Payload EmailNotificationEvent event
    ) {
        emailService.sendEmail(event);
    }
}
