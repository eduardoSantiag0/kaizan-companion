package io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos;


import io.github.eduardosantiag0.sgf.go.SgfCoordinate;

import java.util.List;



public record MoveInformation(
        int order, // para rankear
        List<SgfCoordinate> pv, // Array de [ "P16","K17" ] melhores movimentos
        double playSelectionValue, // valor para calcular order
        double humanPrior
) {
}

//todo Como validar caso seja um pass? Visto que SgfCoordinate lança um erroo
