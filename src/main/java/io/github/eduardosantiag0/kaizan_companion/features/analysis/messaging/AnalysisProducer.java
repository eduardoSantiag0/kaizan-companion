package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisDTO;
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

    private final AnalysisRepository analysisRepository;
    private final RabbitTemplate rabbitTemplate;
    private final String QUEUE;
    private final KatagoQueryBuilder queryBuilder;

    public AnalysisProducer(AnalysisRepository analysisRepository,
                            RabbitTemplate rabbitTemplate,
                            @Value("${rabbitmq.exchange.analysis.queue}")
                            String queue, KatagoQueryBuilder queryBuilder) {
        this.analysisRepository = analysisRepository;
        this.rabbitTemplate = rabbitTemplate;
        QUEUE = queue;
        this.queryBuilder = queryBuilder;
    }


    public void execute(TelegramChatEntity chat, ByteArrayResource fileData) throws IOException {

        byte[] gameBytes = fileData.getContentAsByteArray();

        UUID correlationId = UUID.randomUUID();

        AnalysisJobEntity entity = new AnalysisJobEntity(chat, Instant.now(), correlationId);

        SgfCollection collection = new SgfParser().parse(gameBytes);

        SgfGameTree game = collection.game(0);

        entity.setLinkToSource(String.valueOf(game.root().value("PC")));

        analysisRepository.save(entity);

        KatagoQuery query = queryBuilder.buildQuery(game, correlationId);

        System.out.println("\n\n");
        log.debug(String.valueOf(query));


        AnalysisDTO dto = new AnalysisDTO(
                game,
                entity.getCorrelationId(),
                entity.getChat().getChatId()
                );

        AnalysisCreatedEvent event = new AnalysisCreatedEvent(dto, query);
        rabbitTemplate.convertAndSend(QUEUE, event);

    }

}
