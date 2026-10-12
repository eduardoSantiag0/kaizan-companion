package io.github.eduardosantiag0.kaizan_companion.features.analysis.katago;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.dtos.KatagoQuery;
import io.github.eduardosantiag0.sgf.go.SgfCoordinate;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import io.github.eduardosantiag0.sgf.model.SgfNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class KatagoQueryBuilder {

    private String moveSGFtoGTP(SgfNode move) {
        StringBuilder sBuilder = new StringBuilder();

        if (move.hasProperty("B")) {
            sBuilder.append("play black ");
            String sgfCoord = move.value("B").get();
//            sgfCoord == pc -> [p][c]
            log.error(sgfCoord);
            sBuilder.append(SgfCoordinate.parse(sgfCoord));
        } else if (move.hasProperty("W")) {
            sBuilder.append("play white ");
            String sgfCoord = move.value("W").get();
            sBuilder.append(SgfCoordinate.parse(sgfCoord));
        }
         return sBuilder.toString();
    }

    private List<String> convertMovesSGFtoGTP(List<SgfNode> moves) {
        // 1. Simulando jogadas sequenciais
        // SGF: B[pd] W[dp]  <-- Adiciona duas pedras Pretas
        // GTP: play black P16
        // GTP: play black D4

        return moves.stream()
                .map(this::moveSGFtoGTP)
                .toList();
    }

    private List<String> convertInitialStonesToGTP(SgfNode root) {
        // 2. Configurando o estado inicial do tabuleiro (Sem gerar histórico/passado)
        // SGF: AB[pd][dp]
        // GTP: set_position B P16 B D4

        List<String> initialStones = new ArrayList<>();

        if (!root.hasProperty("AB") && !root.hasProperty("AW")) {
            return null;
        }

        if (root.hasProperty("AB")) {
            root.values("AB").forEach((move ->
                    initialStones.add(
                            SgfCoordinate.parse(move).toString()
                    )));
        }

        if (root.hasProperty("AW")) {
            root.values("AW").forEach((move ->
                    initialStones.add(
                            SgfCoordinate.parse(move).toString()
                    )));
        }

        return  initialStones;
    }

    public KatagoQuery buildQuery(SgfGameTree game, UUID correlationId) {
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


        List<Integer> analyseTurns = new ArrayList<>();
        for (int i = 1; i <= game.moveCount(); i++) {
            analyseTurns.add(i);
        }


        return KatagoQuery.builder()
                .id(String.valueOf(correlationId))
                .initialStone(convertInitialStonesToGTP(game.root()))
                .moves(convertMovesSGFtoGTP(game.moves()))
                .rules(root.value("RU").get())
                .komi(Float.parseFloat(root.value("KM").get()))
                .boardXSize(boardXSize)
                .boardYSize(boardYSize)
                .analyzeTurns(analyseTurns)
                .maxVisits(100)
                .overridingSettings(
                        new KatagoQuery.OverridingSettings(humanSLProfile)
                )
                .build();

    }
}
