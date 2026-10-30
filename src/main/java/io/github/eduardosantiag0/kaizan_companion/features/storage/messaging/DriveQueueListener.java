package io.github.eduardosantiag0.kaizan_companion.features.storage.messaging;

import com.rabbitmq.client.Channel;
import io.github.eduardosantiag0.kaizan_companion.infra.providers.storage.GoogleDriveStorageProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class DriveQueueListener {
    private final GoogleDriveStorageProvider driveStorageProvider;

    public DriveQueueListener(GoogleDriveStorageProvider driveStorageProvider
    ) {
        this.driveStorageProvider = driveStorageProvider;
    }

    @RabbitListener(
            queues = "${rabbitmq.queue.external.drive}"
    )
    public void listen(
            @Payload DriveStorageEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        try {
            driveStorageProvider.uploadFile(event);
            channel.basicAck(deliveryTag, false);
        } catch (RuntimeException e) {
            log.error("Failed to upload drive event for chat {}", event.chatId(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

}
