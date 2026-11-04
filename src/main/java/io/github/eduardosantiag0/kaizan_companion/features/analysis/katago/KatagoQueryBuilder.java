package io.github.eduardosantiag0.kaizan_companion.features.analysis.katago;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.converters.ConverterSGFtoGTP;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.dtos.KatagoQuery;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import io.github.eduardosantiag0.sgf.model.SgfNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class KatagoQueryBuilder {

    public KatagoQuery buildQuery(SgfGameTree game, UUID correlationId) {
        SgfNode root = game.root();

        String whiteRank = String.valueOf(root.value("WR"));
        String blackRank = String.valueOf(root.value("BR"));
        String humanSLProfile;

        if (whiteRank.isEmpty() && !blackRank.isEmpty()) {
            humanSLProfile =
                    "rank_" + blackRank.toLowerCase();
        } else if (blackRank.isEmpty() && !whiteRank.isEmpty()) {
            humanSLProfile =
                    "rank_" + whiteRank.toLowerCase();
        } else {
            humanSLProfile =
                    "rank_" + blackRank.toLowerCase()
                            + "_" + whiteRank.toLowerCase();
        }




        String size = root.value("SZ").orElse("19");
        String[] dimensions = size.split(":");

        int boardXSize = Integer.parseInt(dimensions[0]);
        System.out.println(Arrays.toString(dimensions));
        int boardYSize = dimensions.length > 1
                ? Integer.parseInt(dimensions[1])
                : boardXSize;


        List<Integer> analyseTurns = new ArrayList<>();
        for (int i = 1; i <= game.moveCount(); i++) {
            analyseTurns.add(i);
        }

        float komi = 0;
        if (!root.value("KM").get().isEmpty()) {
            komi = Float.parseFloat(root.value("KM").get());
        }


        return KatagoQuery.builder()
                .id(String.valueOf(correlationId))
                .initialStone(ConverterSGFtoGTP.convertInitialStonesToGTP(game.root(), Integer.parseInt(dimensions[0])))
                .moves(ConverterSGFtoGTP.convertMovesSGFtoGTP(game.moves(), Integer.parseInt(dimensions[0])))
                .rules(root.value("RU").get())
                .komi(komi)
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
