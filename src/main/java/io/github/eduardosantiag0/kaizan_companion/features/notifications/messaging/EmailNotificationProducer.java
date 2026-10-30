package io.github.eduardosantiag0.kaizan_companion.features.notifications.messaging;

import io.github.eduardosantiag0.kaizan_companion.domain.exception.messaging.RabbitMqUnavailableException;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationProducer {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public EmailNotificationProducer(
            RabbitTemplate rabbitTemplate,
            @Value("${rabbitmq.exchange.analysis.exchange}") String exchange,
            @Value("${rabbitmq.queue.external.email.routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void send(EmailNotificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
        } catch (AmqpConnectException e) {
            throw new RabbitMqUnavailableException("RabbitMQ is unavailable", e);
        }
    }
}
