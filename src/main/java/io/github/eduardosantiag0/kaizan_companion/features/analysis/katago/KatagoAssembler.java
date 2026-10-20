package io.github.eduardosantiag0.kaizan_companion.features.analysis.katago;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.MoveInformation;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.converters.ConverterGTPtoSGF;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.runpod.CriticalPoint;
import io.github.eduardosantiag0.sgf.go.GoGame;
import io.github.eduardosantiag0.sgf.model.SgfCollection;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import io.github.eduardosantiag0.sgf.model.SgfNode;
import io.github.eduardosantiag0.sgf.parser.SgfParser;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class KatagoAssembler {

    private final ConverterGTPtoSGF gtpToSGF;

    private final SgfParser sgfParser = new SgfParser();

    public KatagoAssembler(ConverterGTPtoSGF gttoSGF) {
        this.gtpToSGF = gttoSGF;
    }

    private ByteArrayResource attach(CriticalPoint criticalPoint, String originalGame) {

        // 1. Pegar o primeiro Move Information
        List<String> bestSequenceInThisTurn = criticalPoint.before().moveInfos()
                .stream()
                .min(Comparator.comparingInt(MoveInformation::order))
                .map(MoveInformation::pv)
                .orElseGet(List::of);


        // Converter para SGF
        ConverterGTPtoSGF.getSgfFormat()



        // 2. Colocar na arvore original e retornar

        SgfCollection collection = sgfParser.parse(originalGame);

        SgfGameTree gameTree = collection.games().get(0);

        // Pegar o node
        SgfNode pointToAnalyse = gameTree.root();

        pointToAnalyse.addAlternative();
        // Adicionar as variações

    }

    public ByteArrayResource attachAnalysisToGame(CriticalPoint criticalPoint, String originalGame) {

        SgfGameTree analysedTree = ConverterGTPtoSGF.getSgfFormat(criticalPoint);

        var fileData = attach(analysedTree, originalGame);

        return new ByteArrayResource(fileData.toString().getBytes());

    }

    public ByteArrayResource assebleAllPhases(List<ByteArrayResource> gamePhases) {

    }
}
