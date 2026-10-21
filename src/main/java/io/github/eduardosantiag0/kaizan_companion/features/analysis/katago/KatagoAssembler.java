package io.github.eduardosantiag0.kaizan_companion.features.analysis.katago;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.MoveInformation;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.converters.ConverterGTPtoSGF;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.runpod.CriticalPoint;
import io.github.eduardosantiag0.sgf.go.*;
import io.github.eduardosantiag0.sgf.model.SgfCollection;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import io.github.eduardosantiag0.sgf.model.SgfNode;
import io.github.eduardosantiag0.sgf.parser.SgfParser;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class KatagoAssembler {

    private final ConverterGTPtoSGF gtpToSGF;

    private static final SgfParser sgfParser = new SgfParser();

    public KatagoAssembler(ConverterGTPtoSGF gttoSGF) {
        this.gtpToSGF = gttoSGF;
    }

    private static SgfNode attach(CriticalPoint criticalPoint, String originalGame) {

        SgfCollection collection = sgfParser.parse(originalGame);
        SgfGameTree gameTree = collection.firstGame();

        List<String> bestSequenceInThisTurn = criticalPoint.before().moveInfos()
                .stream()
                .min(Comparator.comparingInt(MoveInformation::order))
                .map(MoveInformation::pv)
                .orElseGet(List::of);


        //TODO fix board size
//        List<SgfCoordinate> coordinates = bestSequenceInThisTurn.stream()
//                .map(s -> SgfCoordinate.fromHuman(s, 19))
//                .toList();

        //TODO pegar todas as pedras e colocar dcomo setup


        SgfNode originalMove = gameTree.getMove(criticalPoint.turn());

        StoneColor color = SgfMove.from(originalMove, 19)
                .orElseThrow()
                .color();

        List<SgfMove> moves = new ArrayList<>();

        for (String position : bestSequenceInThisTurn) {
            SgfMove move;

            if (position.equalsIgnoreCase("pass")) {
                move = SgfMove.pass(color);
            } else {
                SgfCoordinate coordinate =
                        SgfCoordinate.fromHuman(position, 19);

                move = SgfMove.of(color, coordinate);
            }

            moves.add(move);
            color = color.opposite();
        }

        return originalMove.addAlternative(SgfMove.toNodes(moves));

    }

    public String attachAnalysisToGame(CriticalPoint criticalPoint, String originalGame) {
        return attach(criticalPoint, originalGame).toString();
    }

//    public ByteArrayResource assebleAllPhases(List<ByteArrayResource> gamePhases) {
//
//    }
}
