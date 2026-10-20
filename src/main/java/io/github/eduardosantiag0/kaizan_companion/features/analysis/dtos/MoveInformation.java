package io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;



@JsonIgnoreProperties(ignoreUnknown = true)
public record MoveInformation(
        int order, // para rankear
        List<String> pv, // Array de [ "P16","K17" ] melhores movimentos, em notação humana/GTP
        double playSelectionValue, // valor para calcular order
        double humanPrior
) {
}

// Para converter uma entrada em SgfCoordinate, use SgfCoordinate.fromHuman(valor, boardSize).
