package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import io.debezium.engine.ChangeEvent;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.OUTBOX_EVENTS;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.OutboxRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;


@Slf4j
@Service
public class OutboxPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper mapper;
    private final String analysisExchange;
    private final String analysisRoutingKey;

    public OutboxPublisher(
            RabbitTemplate rabbitTemplate,
            OutboxRepository outboxRepository,
            ObjectMapper mapper,
            @Value("${rabbitmq.exchange.analysis.exchange}") String analysisExchange,
            @Value("${rabbitmq.exchange.analysis.routing-key}") String analysisRoutingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.outboxRepository = outboxRepository;
        this.mapper = mapper;
        this.analysisExchange = analysisExchange;
        this.analysisRoutingKey = analysisRoutingKey;
    }

    public void handleChangeEvent(ChangeEvent<String, String> changeEvent) {
        String value = changeEvent.value();

        if (value == null) {
            return;
        }

        JsonNode envelope = mapper.readTree(value);

        // insert (op = "c")
        if (!"c".equals(envelope.path("op").asString(""))) {
            return;
        }

        JsonNode after = envelope.get("after");
        UUID id = UUID.fromString(after.get("id").asString());
        String aggregateType = after.get("aggregate_type").asString();

        if (!"Analysis".equals(aggregateType)) {
            log.warn("Outbox event {} com aggregate_type desconhecido: {}", id, aggregateType);
            return;
        }

        try {
            AnalysisCreatedEvent event = mapper.readValue(
                    after.get("payload").asString(), AnalysisCreatedEvent.class
            );
            rabbitTemplate.convertAndSend(analysisExchange, analysisRoutingKey, event);
            markAsDone(id);

            log.info("Outbox event {} publicado", id);
        } catch (RuntimeException e) {
            log.error("Falha ao publicar outbox event {}", id, e);
            throw e;
        }
    }

    private void markAsDone(UUID id) {
        outboxRepository.findById(id).ifPresentOrElse(
                outboxEvent -> {
                    outboxEvent.setType(OUTBOX_EVENTS.DONE);
                    outboxEvent.setPublished(true);
                    outboxRepository.save(outboxEvent);
                },
                () -> log.warn("Outbox event {} não encontrado para marcar como DONE", id)
        );
    }
}
