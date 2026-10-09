package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisDTO;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.dtos.KatagoQuery;
import io.github.eduardosantiag0.sgf.model.SgfCollection;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import io.github.eduardosantiag0.sgf.model.SgfNode;
import io.github.eduardosantiag0.sgf.parser.SgfParser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class AnalysisProducer {

    private final AnalysisRepository analysisRepository;
    private final RabbitTemplate rabbitTemplate;
    private final String QUEUE;

    public AnalysisProducer(AnalysisRepository analysisRepository,
                            RabbitTemplate rabbitTemplate,
                            @Value("${rabbitmq.exchange.analysis.queue}")
                            String queue) {
        this.analysisRepository = analysisRepository;
        this.rabbitTemplate = rabbitTemplate;
        QUEUE = queue;
    }


    //todo Fazer essa função
    private KatagoQuery buildKatagoQuery(SgfGameTree game, UUID correlationId) {
        SgfNode root = game.root();

        String whiteRank = root.value("WR").orElseThrow();
        String blackRank = root.value("BR").orElseThrow();

        String humanSLProfile =
                "rank_" + blackRank.toLowerCase()
                        + "_"
                        + whiteRank.toLowerCase();

        String size = root.value("SZ").orElse("19");
        String[] dimensions = size.split(":");

        int boardXSize = Integer.parseInt(dimensions[0]);
        int boardYSize = dimensions.length > 1
                ? Integer.parseInt(dimensions[1])
                : boardXSize;

        //todo AnalyseTurn
        //todo converter jogadas SGF para GTP nos moves
        //todo initialStones em converter AB e AW

        return KatagoQuery.builder()
                .id(String.valueOf(correlationId))
                .initialStones(root.value("AB")) /* converter AB e AW */
                .moves(game.moves()) /* converter jogadas SGF para GTP */
                .rules(String.valueOf(root.value("RU")))
                .komi(root.value("KM"))
                .boardXSize(boardXSize)
                .boardYSize(boardYSize)
                .analyseTurns(analyzeTurns)
                .maxVisits(100)
                .overrideSettings(
                        new KatagoQuery.OverridingSettings(humanSLProfile)
                )
                .build();

    }


    public void execute(TelegramChatEntity chat, ByteArrayResource fileData) throws IOException {

        byte[] gameBytes = fileData.getContentAsByteArray();

        UUID correlationId = UUID.randomUUID();

        AnalysisJobEntity entity = new AnalysisJobEntity(chat, Instant.now(), correlationId);

        SgfCollection collection = new SgfParser().parse(gameBytes);

        SgfGameTree game = collection.game(0);

        entity.setLinkToSource(String.valueOf(game.root().value("PC")));

        analysisRepository.save(entity);

        KatagoQuery query = buildKatagoQuery (game, correlationId);


        AnalysisDTO dto = new AnalysisDTO(
                game,
                entity.getCorrelationId(),
                entity.getChat().getChatId()
                );

        AnalysisCreatedEvent event = new AnalysisCreatedEvent(dto, query);
        rabbitTemplate.convertAndSend(QUEUE, event);

    }

}
