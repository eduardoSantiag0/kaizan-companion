package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.analysis.NullAnalysisQueryException;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.messaging.RabbitMqUnavailableException;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisDTO;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.JobStatus;
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
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class AnalysisProducer {

    private final AnalysisJobRepository analysisJobRepository;
    private final RabbitTemplate rabbitTemplate;
    private final String QUEUE;
    private final KatagoQueryBuilder queryBuilder;

    public AnalysisProducer(AnalysisJobRepository analysisJobRepository,
                            RabbitTemplate rabbitTemplate,
                            @Value("${rabbitmq.exchange.analysis.queue}")
                            String queue, KatagoQueryBuilder queryBuilder) {
        this.analysisJobRepository = analysisJobRepository;
        this.rabbitTemplate = rabbitTemplate;
        QUEUE = queue;
        this.queryBuilder = queryBuilder;
    }


    public void execute(TelegramChatEntity chat, ByteArrayResource fileData) throws SgfParseException, IOException {

        byte[] gameBytes = fileData.getContentAsByteArray();

        UUID correlationId = UUID.randomUUID();

        AnalysisJobEntity jobEntity = new AnalysisJobEntity(chat, Instant.now(), correlationId);

        SgfCollection collection;

        try {
            collection = new SgfParser().parse(gameBytes);
        } catch (SgfParseException e) {
            throw new SgfParseException(e.getMessage(), e.position(), e.line(), e.column());
        }

        SgfGameTree game = collection.game(0);

        jobEntity.setLinkToSource(String.valueOf(game.root().value("PC")));
        jobEntity.setOriginalGame(new String(gameBytes, StandardCharsets.UTF_8));


        KatagoQuery query = queryBuilder.buildQuery(game, correlationId);

        if (query == null) {
            throw  new NullAnalysisQueryException("Query is null");
        }

        AnalysisDTO dto = new AnalysisDTO (
                jobEntity.getCorrelationId(),
                jobEntity.getChat().getChatId()
        );

        AnalysisCreatedEvent event = new AnalysisCreatedEvent(dto, query);
        jobEntity.setJobStatus(JobStatus.ANALYSIS_REQUESTED);
        analysisJobRepository.save(jobEntity);

        try {
            rabbitTemplate.convertAndSend(QUEUE, event);
        } catch (AmqpConnectException e) {
            throw new RabbitMqUnavailableException(
                    "RabbitMQ is unavailable",
                    e
            );
        }

    }

}
