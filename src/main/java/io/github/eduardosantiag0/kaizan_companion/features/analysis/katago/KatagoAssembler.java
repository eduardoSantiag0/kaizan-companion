package io.github.eduardosantiag0.kaizan_companion.features.analysis.katago;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.MoveInformation;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.converters.ConverterGTPtoSGF;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.runpod.CriticalPoint;
import io.github.eduardosantiag0.sgf.go.*;
import io.github.eduardosantiag0.sgf.model.SgfCollection;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import io.github.eduardosantiag0.sgf.model.SgfNode;
import io.github.eduardosantiag0.sgf.model.SgfProperty;
import io.github.eduardosantiag0.sgf.parser.SgfParser;
import io.github.eduardosantiag0.sgf.serializer.SgfSerializer;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class KatagoAssembler {

    private static final int BEST_CANDIDATES_LIMIT = 5;
    private static final int VARIATION_MOVES_LIMIT = 6;

    private static final SgfParser sgfParser = new SgfParser();

    public KatagoAssembler() {
    }

    private static Map<SgfCoordinate, StoneColor> reconstructBoard(GoPosition positionBefore, int boardSize) {
        Map<SgfCoordinate, StoneColor> board = new LinkedHashMap<>();

        for (SgfMove stone : positionBefore.setupStones()) {
            stone.coordinate().ifPresent(coordinate -> board.put(coordinate, stone.color()));
        }

        for (SgfMove move : positionBefore.moves()) {
            move.coordinate().ifPresent(point -> {
                board.put(point, move.color());
            });
        }
        return board;
    }


    private static SgfNode buildSetupRoot(SgfGameTree originalTree, GoGame goGame, SgfNode originalMoveNode, StoneColor toMove) {
        int boardSize = goGame.boardSize();

        GoPosition positionBefore = goGame.positionBefore(originalMoveNode);

        Map<SgfCoordinate, StoneColor> board = reconstructBoard(positionBefore, boardSize);

        SgfNode newRoot = new SgfNode();

        for (SgfProperty property : originalTree.root().properties()) {
            String identifier = property.identifier();

            if (!identifier.equals("AB")
                    && !identifier.equals("AW")
                    && !identifier.equals("AE")
                    && !identifier.equals("PL"))
            {
                newRoot.setProperty(identifier, property.values());
            }
        }

        List<String> blackSetup = new ArrayList<>();
        List<String> whiteSetup = new ArrayList<>();


        board.forEach((coordinate, color) -> {
            String point = SgfMove.of(color, coordinate).toSgfValue();
            (color == StoneColor.BLACK ? blackSetup : whiteSetup).add(point);
        });


        if (!blackSetup.isEmpty()) {
            newRoot.setProperty("AB", blackSetup);
        }

        if (!whiteSetup.isEmpty()) {
            newRoot.setProperty("AW", whiteSetup);
        }

        newRoot.setProperty("PL", toMove.sgfIdentifier());

        return newRoot;
    }

    private static List<SgfMove> toAlternatingMoves(List<String> humanPositions, StoneColor startingColor, int boardSize) {
        StoneColor color = startingColor;
        List<SgfMove> moves = new ArrayList<>();

        for (String position : humanPositions) {
            SgfMove move;

            if (position.equalsIgnoreCase("pass")) {
                move = SgfMove.pass(color);
            } else {
                SgfCoordinate coordinate = SgfCoordinate.fromHuman(position, boardSize);
                move = SgfMove.of(color, coordinate);
            }

            moves.add(move);
            color = color.opposite();
        }

        return moves;
    }

    private static SgfGameTree attach(CriticalPoint criticalPoint, String originalGame) {

        SgfCollection collection = sgfParser.parse(originalGame);
        SgfGameTree originalTree = collection.firstGame();

        GoGame goGame = GoGame.of(originalTree);
        int boardSize = goGame.boardSize();

        SgfNode originalMoveNode = originalTree.getMove(criticalPoint.turn());

        SgfMove originalMove = SgfMove.from(originalMoveNode, boardSize).orElseThrow();

        SgfNode newRoot = buildSetupRoot(originalTree, goGame, originalMoveNode, originalMove.color());

        List<MoveInformation> bestCandidates = criticalPoint.before().moveInfos()
                .stream()
                .sorted(Comparator.comparingInt(MoveInformation::order))
                .limit(BEST_CANDIDATES_LIMIT)
                .toList();

        for (MoveInformation candidate : bestCandidates) {
            List<String> limitedPv = candidate
                    .pv()
                    .stream()
                    .limit(VARIATION_MOVES_LIMIT)
                    .toList();

            List<SgfMove> candidateMoves = toAlternatingMoves(limitedPv, originalMove.color(), boardSize);

            if (!candidateMoves.isEmpty()) {
                newRoot.addVariation(SgfMove.toNodes(candidateMoves));
            }
        }

        return new SgfGameTree(newRoot);
    }


    public String attachAnalysisToGame(CriticalPoint criticalPoint, String originalGame) {
        return SgfSerializer.pretty().serialize(attach(criticalPoint, originalGame));
    }

//    public ByteArrayResource assebleAllPhases(List<ByteArrayResource> gamePhases) {
//
//    }
}
