package io.github.eduardosantiag0.kaizan_companion.features.analysis.katago;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.runpod.dtos.GameAnalysisResponse;
import io.github.eduardosantiag0.sgf.model.SgfCollection;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import org.springframework.core.io.ByteArrayResource;

public class KatagoAssembler {

    private SgfGameTree getSgfFormat (String s) {

    }

    public ByteArrayResource execute (GameAnalysisResponse response, String s) {

        SgfGameTree originalGame = getSgfFormat(s);

        // Pegar game move
        // Adicionar variação na jogada
        //

        // Exportar como ByyrArray
    }
}
