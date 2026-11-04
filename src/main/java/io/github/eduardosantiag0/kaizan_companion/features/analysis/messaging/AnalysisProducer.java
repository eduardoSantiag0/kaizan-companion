package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import io.github.eduardosantiag0.kaizan_companion.domain.exception.analysis.NullAnalysisQueryException;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.messaging.RabbitMqUnavailableException;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.JobSubmissionService;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisDTO;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.KatagoQueryBuilder;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.dtos.KatagoQuery;
import io.github.eduardosantiag0.kaizan_companion.utils.SgfRepair;
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

    private static final int MAX_REPAIR_ATTEMPTS = 3;

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
        String sgfContent = new String(gameBytes, StandardCharsets.UTF_8);

        UUID correlationId = UUID.randomUUID();
        SgfCollection collection;

        try {
            collection = new SgfParser().parse(gameBytes);
        } catch (SgfParseException e) {
            collection = repairAndParse(sgfContent, e);
        }

        SgfGameTree game = collection.game(0);

        KatagoQuery query = queryBuilder.buildQuery(game, correlationId);

        if (query == null) {
            throw new NullAnalysisQueryException("Query is null");
        }

        AnalysisDTO dto = new AnalysisDTO(correlationId, chatId);

        AnalysisCreatedEvent event = new AnalysisCreatedEvent(dto, query);

        jobSubmissionService.saveJob(
                chatId,
                correlationId,
                String.valueOf(game.root().value("PC")),
                new String(gameBytes, StandardCharsets.UTF_8),
                event
        );

    }

    private SgfCollection repairAndParse(String sgfContent, SgfParseException error) {
        for (int attempt = 1; attempt <= MAX_REPAIR_ATTEMPTS; attempt++) {
            sgfContent = SgfRepair.repair(error, sgfContent);

            try {
                return new SgfParser().parse(sgfContent.getBytes(StandardCharsets.UTF_8));
            } catch (SgfParseException e) {
                log.warn("Tentativa {} de repair do SGF falhou: {}", attempt, e.getMessage());
                error = e;
            }
        }

        throw error;
    }
}
