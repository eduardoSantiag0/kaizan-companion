package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.OUTBOX_EVENTS;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.OutboxRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Slf4j
@Service
public class OutboxPublisher implements DebeziumEngine.ChangeConsumer<ChangeEvent<String, String>> {

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

    @Override
    public void handleBatch(
            List<ChangeEvent<String, String>> records,
            DebeziumEngine.RecordCommitter<ChangeEvent<String, String>> committer
    ) throws InterruptedException {

        record Result(ChangeEvent<String, String> record, Throwable error) {}

        List<Future<Result>> futures = new ArrayList<>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (ChangeEvent<String, String> record : records) {
                futures.add(executor.submit(() -> {
                    try {
                        processChangeEvent(record);
                        return new Result(record, null);
                    } catch (RuntimeException e) {
                        return new Result(record, e);
                    }
                }));
            }
        }

        for (int i = 0; i < futures.size(); i++) {
            ChangeEvent<String, String> record = records.get(i);
            Throwable error;

            try {
                error = futures.get(i).get().error();
            } catch (ExecutionException e) {
                error = e.getCause();
            }

            if (error != null) {
                log.error("Falha ao processar outbox event, será reentregue", error);
                break; // para de marcar a partir do primeiro erro — preserva a ordem do LSN
            }
            committer.markProcessed(record);
        }

        committer.markBatchFinished();
    }

    private void processChangeEvent(ChangeEvent<String, String> changeEvent) {
        String value = changeEvent.value();

        if (value == null) {
            return;
        }

        log.debug("Dentro do handle change event");

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
        int updated = outboxRepository.markAsDone(id, OUTBOX_EVENTS.DONE);
        if (updated == 0) {
            log.warn("Outbox event {} não encontrado para marcar como DONE", id);
        }
    }
}
