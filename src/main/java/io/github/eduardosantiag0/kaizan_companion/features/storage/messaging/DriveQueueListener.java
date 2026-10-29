package io.github.eduardosantiag0.kaizan_companion.features.storage.messaging;

import io.github.eduardosantiag0.kaizan_companion.infra.providers.storage.GoogleDriveStorageProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DriveQueueListener {
    private final GoogleDriveStorageProvider driveStorageProvider;

    public DriveQueueListener(GoogleDriveStorageProvider driveStorageProvider) {
        this.driveStorageProvider = driveStorageProvider;
    }

    @RabbitListener(
            queues = "${rabbitmq.queue.external.drive}"
    )
    public void listen(
            @Payload DriveStorageEvent event
    ) {

    }

}
