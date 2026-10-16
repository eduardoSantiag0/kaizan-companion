package io.github.eduardosantiag0.kaizan_companion.features.analysis.converters;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.GTPTablePosition;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.runpod.dtos.CriticalPoint;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import io.github.eduardosantiag0.sgf.model.SgfNode;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConverterGTPtoSGF {

    private static final int DEFAULT_BOARD_SIZE = 19;

    private static String convertSingleGtpToSgf(String gtpCoord, int boardYSize) {
        //// GTP: Q16 -> SGF: pd

        if (gtpCoord == null || gtpCoord.isBlank() || gtpCoord.equalsIgnoreCase("pass")) {
            return "";
        }

        String gtpCol = String.valueOf(Character.toUpperCase(gtpCoord.charAt(0)));

        String rawCol = gtpCol;
        for (Map.Entry<String, String> entry : GTPTablePosition.POSITIONS.entrySet()) {
            if (entry.getValue().equals(gtpCol)) {
                rawCol = entry.getKey();
                break;
            }
        }

        char colChar = (char) ('a' + (rawCol.charAt(0) - 'A'));

        int gtpRow = Integer.parseInt(gtpCoord.substring(1));
        int sgfRow = boardYSize - gtpRow;
        char rowChar = (char) ('a' + sgfRow);

        return "" + colChar + rowChar;
    }

    private static SgfNode moveGTPtoSGF(String player, String move, int boardYSize) {
        String sgfCoord = convertSingleGtpToSgf(move, boardYSize);

        SgfNode node = new SgfNode();

        if (player.equalsIgnoreCase("b") || player.equalsIgnoreCase("black")) {
            node.setProperty("B", sgfCoord);
        } else if (player.equalsIgnoreCase("w") || player.equalsIgnoreCase("white")) {
            node.setProperty("W", sgfCoord);
        }

        return node;
    }

    //TODO deve aceitar todos os formatos de tabuleiro
    public static SgfGameTree getSgfFormat(CriticalPoint s) {
        SgfNode root = moveGTPtoSGF(s.player(), s.move(), DEFAULT_BOARD_SIZE);

        return new SgfGameTree(root);
    }

}
