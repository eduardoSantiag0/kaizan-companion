package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import io.github.eduardosantiag0.kaizan_companion.domain.exception.analysis.NullAnalysisQueryException;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.messaging.RabbitMqUnavailableException;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.JobSubmissionService;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisDTO;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.KatagoQueryBuilder;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.dtos.KatagoQuery;
import io.github.eduardosantiag0.sgf.model.SgfCollection;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import io.github.eduardosantiag0.sgf.parser.SgfParseException;
import io.github.eduardosantiag0.sgf.parser.SgfParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
@Slf4j
@Service
public class AnalysisProducer {

    private final JobSubmissionService jobSubmissionService;
    private final KatagoQueryBuilder queryBuilder;
    private final RabbitTemplate rabbitTemplate;
    private final String queue;

    public AnalysisProducer(
            JobSubmissionService jobSubmissionService,
            KatagoQueryBuilder queryBuilder,
            RabbitTemplate rabbitTemplate,
            @Value("${rabbitmq.queue.analysis}") String queue
    ) {
        this.jobSubmissionService = jobSubmissionService;
        this.queryBuilder = queryBuilder;
        this.rabbitTemplate = rabbitTemplate;
        this.queue = queue;
    }

    public void execute(Long chatId, ByteArrayResource fileData)
            throws SgfParseException, IOException {

        byte[] gameBytes = fileData.getContentAsByteArray();

        UUID correlationId = UUID.randomUUID();

        SgfCollection collection = new SgfParser().parse(gameBytes);
        SgfGameTree game = collection.game(0);

        KatagoQuery query = queryBuilder.buildQuery(game, correlationId);

        if (query == null) {
            throw new NullAnalysisQueryException("Query is null");
        }

        AnalysisDTO dto = new AnalysisDTO(correlationId, chatId);

        jobSubmissionService.saveJob(
                chatId,
                correlationId,
                String.valueOf(game.root().value("PC")),
                new String(gameBytes, StandardCharsets.UTF_8)
        );

        AnalysisCreatedEvent event = new AnalysisCreatedEvent(dto, query);

        try {
            rabbitTemplate.convertAndSend(queue, event);
        } catch (AmqpConnectException e) {
            throw new RabbitMqUnavailableException(
                    "RabbitMQ is unavailable",
                    e
            );
        }
    }
}
