package io.github.eduardosantiag0.kaizan_companion.features.notifications.messaging;

import com.rabbitmq.client.Channel;
import io.github.eduardosantiag0.kaizan_companion.infra.providers.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
            @Payload EmailNotificationEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        emailService.sendEmail(event);
        channel.basicAck(deliveryTag, false);
    }
}
