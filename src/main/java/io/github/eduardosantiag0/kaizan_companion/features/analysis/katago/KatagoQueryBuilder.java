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
//
//    private List<String> moveSGFtoGTP(SgfNode move) {
//
//        if (move.hasProperty("B")) {
//            return List.of("b", move.value("B").get());
//
//////            sgfCoord == pc -> [p][c]
//
//        } else if (move.hasProperty("W")) {
//            return List.of("w", move.value("W").get());
//
//        }
//        return null;
//    }
//
//
//    private List<List<String>> convertMovesSGFtoGTP(List<SgfNode> moves, int boardYSize) {
//        //// SGF: B[pd] W[dp]
//        //// Retorno para a KataGo: [["B", "Q16"], ["W", "D4"]]
//
//        // A lista de jogadas.
//            // Jogadas  sequenciais.
//                // O par GTP):
//                    // ["black", "Q16"], ["white", "D4"]
//
//        return moves.stream()
//                .map(this::moveSGFtoGTP)
//                .filter(Objects::nonNull)
//                .map(move -> {
//                    String color = move.get(0).toUpperCase();
//                    String sgfCoord = move.get(1);
//
//                    if (sgfCoord.isEmpty() || sgfCoord.equals("tt")) {
//                        return List.of(color, "pass");
//                    }
//
//                    char colChar = sgfCoord.charAt(0);
//                    String gtpCol = String.valueOf((char) ('A' + (colChar - 'a')));
//
//                    gtpCol = GTPTablePosition.POSITIONS.getOrDefault(gtpCol, gtpCol);
//
//                    char rowChar = sgfCoord.charAt(1);
//                    int gtpRow = boardYSize - (rowChar - 'a');
//
//                    String gtpCoord = gtpCol + gtpRow;
//
//                    return List.of(color, gtpCoord);
//                })
//                .toList();
//    }
//
//    private List<List<String>> convertInitialStonesToGTP(SgfNode root, int boardYSize) {
//        // SGF: AB[pd][dp] AW[dd]
//        // Retorno para KataGo: [["B", "Q16"], ["B", "D4"], ["W", "D16"]]
//
//        if (!root.hasProperty("AB") && !root.hasProperty("AW")) {
//            return List.of(); // Retornar lista vazia é mais seguro que null para APIs JSON
//        }
//
//        List<List<String>> initialStones = new ArrayList<>();
//
//        if (root.hasProperty("AB")) {
//            root.values("AB").forEach(sgfCoord -> {
//                String gtpCoord = convertSingleSgfToGtp(sgfCoord, boardYSize);
//                if (gtpCoord != null) {
//                    initialStones.add(List.of("B", gtpCoord));
//                }
//            });
//        }
//
//        if (root.hasProperty("AW")) {
//            root.values("AW").forEach(sgfCoord -> {
//                String gtpCoord = convertSingleSgfToGtp(sgfCoord, boardYSize);
//                if (gtpCoord != null) {
//                    initialStones.add(List.of("W", gtpCoord));
//                }
//            });
//        }
//
//        return initialStones;
//    }
//
//    private String convertSingleSgfToGtp(String sgfCoord, int boardYSize) {
//        if (sgfCoord == null || sgfCoord.length() < 2) {
//            return null;
//        }
//
//        char colChar = sgfCoord.charAt(0);
//        String gtpCol = String.valueOf((char) ('A' + (colChar - 'a')));
//
//        gtpCol = GTPTablePosition.POSITIONS.getOrDefault(gtpCol, gtpCol);
//
//        char rowChar = sgfCoord.charAt(1);
//        int gtpRow = boardYSize - (rowChar - 'a');
//
//        return gtpCol + gtpRow;
//    }

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
                .initialStone(ConverterSGFtoGTP.convertInitialStonesToGTP(game.root(), Integer.parseInt(size)))
                .initialStone(ConverterSGFtoGTP.convertInitialStonesToGTP(game.root(), Integer.parseInt(size)))
                .moves(ConverterSGFtoGTP.convertMovesSGFtoGTP(game.moves(), Integer.parseInt(size)))
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
