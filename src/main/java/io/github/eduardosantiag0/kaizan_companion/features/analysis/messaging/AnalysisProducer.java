package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisDTO;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.KatagoQuery;
import io.github.eduardosantiag0.sgf.model.SgfCollection;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import io.github.eduardosantiag0.sgf.model.SgfNode;
import io.github.eduardosantiag0.sgf.parser.SgfParser;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Service
public class AnalysisProducer {

    private final AnalysisRepository analysisRepository;

    public AnalysisProducer(AnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    //todo Fazer essa função
    private KatagoQuery buildKatagoQuery(byte[] gameBytes) {
        SgfCollection collection = new SgfParser().parse(gameBytes);
        SgfGameTree game = collection.game(0);
        SgfNode root = game.root();

        String black = root.value("PB").orElse("desconhecido");
        String white = root.value("PW").orElse("desconhecido");

    }


    public void execute(TelegramChatEntity chat, ByteArrayResource fileData) throws IOException {

        byte[] gameBytes = fileData.getContentAsByteArray();

        UUID correlationId = UUID.randomUUID();

        AnalysisJobEntity entity = new AnalysisJobEntity(chat, Instant.now(), correlationId);

        analysisRepository.save(entity);


        KatagoQuery query = buildKatagoQuery (gameBytes);


        AnalysisDTO dto = new AnalysisDTO(
                entity.getCorrelationId(),
                entity.getChat().getChatId()
                );

        AnalysisCreatedEvent event = new AnalysisCreatedEvent(dto, query);

        //todo Publicar na fila

    }

}
