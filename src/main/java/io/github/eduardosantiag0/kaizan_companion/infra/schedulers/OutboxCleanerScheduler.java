package io.github.eduardosantiag0.kaizan_companion.infra.schedulers;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.OUTBOX_EVENTS;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.OutboxRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxCleanerScheduler {

    private final OutboxRepository outboxRepository;

    public OutboxCleanerScheduler(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Scheduled(cron = "0 0 0 * * MON")
    public void cleanOutboxSentEvents() {
        outboxRepository.deleteByType(OUTBOX_EVENTS.DONE);
    }

}
