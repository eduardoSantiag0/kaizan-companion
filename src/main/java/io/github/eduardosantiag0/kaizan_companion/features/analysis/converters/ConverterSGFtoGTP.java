package io.github.eduardosantiag0.kaizan_companion.features.analysis.converters;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.GTPTablePosition;
import io.github.eduardosantiag0.sgf.model.SgfNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConverterSGFtoGTP {

    public static List<String> moveSGFtoGTP(SgfNode move) {

        if (move.hasProperty("B")) {
            return List.of("b", move.value("B").get());

////            sgfCoord == pc -> [p][c]

        } else if (move.hasProperty("W")) {
            return List.of("w", move.value("W").get());

        }
        return null;
    }


    public static List<List<String>> convertMovesSGFtoGTP(List<SgfNode> moves, int boardYSize) {
        //// SGF: B[pd] W[dp]
        //// Retorno para a KataGo: [["B", "Q16"], ["W", "D4"]]

        // A lista de jogadas.
        // Jogadas  sequenciais.
        // O par GTP):
        // ["black", "Q16"], ["white", "D4"]

        return moves.stream()
                .map(ConverterSGFtoGTP::moveSGFtoGTP)
                .filter(Objects::nonNull)
                .map(move -> {
                    String color = move.get(0).toUpperCase();
                    String sgfCoord = move.get(1);

                    if (sgfCoord.isEmpty() || sgfCoord.equals("tt")) {
                        return List.of(color, "pass");
                    }

                    char colChar = sgfCoord.charAt(0);
                    String gtpCol = String.valueOf((char) ('A' + (colChar - 'a')));

                    gtpCol = GTPTablePosition.POSITIONS.getOrDefault(gtpCol, gtpCol);

                    char rowChar = sgfCoord.charAt(1);
                    int gtpRow = boardYSize - (rowChar - 'a');

                    String gtpCoord = gtpCol + gtpRow;

                    return List.of(color, gtpCoord);
                })
                .toList();
    }

    public static List<List<String>> convertInitialStonesToGTP(SgfNode root, int boardYSize) {
        // SGF: AB[pd][dp] AW[dd]
        // Retorno para KataGo: [["B", "Q16"], ["B", "D4"], ["W", "D16"]]

        if (!root.hasProperty("AB") && !root.hasProperty("AW")) {
            return List.of(); // Retornar lista vazia é mais seguro que null para APIs JSON
        }

        List<List<String>> initialStones = new ArrayList<>();

        if (root.hasProperty("AB")) {
            root.values("AB").forEach(sgfCoord -> {
                String gtpCoord = convertSingleSgfToGtp(sgfCoord, boardYSize);
                if (gtpCoord != null) {
                    initialStones.add(List.of("B", gtpCoord));
                }
            });
        }

        if (root.hasProperty("AW")) {
            root.values("AW").forEach(sgfCoord -> {
                String gtpCoord = convertSingleSgfToGtp(sgfCoord, boardYSize);
                if (gtpCoord != null) {
                    initialStones.add(List.of("W", gtpCoord));
                }
            });
        }

        return initialStones;
    }

    public static String convertSingleSgfToGtp(String sgfCoord, int boardYSize) {
        if (sgfCoord == null || sgfCoord.length() < 2) {
            return null;
        }

        char colChar = sgfCoord.charAt(0);
        String gtpCol = String.valueOf((char) ('A' + (colChar - 'a')));

        gtpCol = GTPTablePosition.POSITIONS.getOrDefault(gtpCol, gtpCol);

        char rowChar = sgfCoord.charAt(1);
        int gtpRow = boardYSize - (rowChar - 'a');

        return gtpCol + gtpRow;
    }

}
