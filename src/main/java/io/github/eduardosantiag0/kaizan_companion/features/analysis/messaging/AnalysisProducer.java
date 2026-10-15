package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisDTO;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.JobStatus;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.KatagoQueryBuilder;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.dtos.KatagoQuery;
import io.github.eduardosantiag0.sgf.model.SgfCollection;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import io.github.eduardosantiag0.sgf.parser.SgfParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
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


    public void execute(TelegramChatEntity chat, ByteArrayResource fileData) throws IOException {

        byte[] gameBytes = fileData.getContentAsByteArray();

        UUID correlationId = UUID.randomUUID();

        AnalysisJobEntity jobEntity = new AnalysisJobEntity(chat, Instant.now(), correlationId);

        SgfCollection collection = new SgfParser().parse(gameBytes);

        SgfGameTree game = collection.game(0);

        jobEntity.setLinkToSource(String.valueOf(game.root().value("PC")));
        jobEntity.setSgfGame(String.valueOf(game));

        analysisJobRepository.save(jobEntity);

        KatagoQuery query = queryBuilder.buildQuery(game, correlationId);

        AnalysisDTO dto = new AnalysisDTO(
                jobEntity.getCorrelationId(),
                jobEntity.getChat().getChatId()
                );

        AnalysisCreatedEvent event = new AnalysisCreatedEvent(dto, query);
        jobEntity.setJobStatus(JobStatus.ANALYSIS_REQUESTED);
        rabbitTemplate.convertAndSend(QUEUE, event);

    }

}
