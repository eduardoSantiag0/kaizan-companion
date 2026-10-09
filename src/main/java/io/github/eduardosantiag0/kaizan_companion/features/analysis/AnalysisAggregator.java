package io.github.eduardosantiag0.kaizan_companion.features.analysis;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.runpod.dtos.GameAnalysisResponse;
import org.springframework.stereotype.Service;

@Service
public class AnalysisAggregator {
    public void handleResponse(GameAnalysisResponse response) {
        // TODO: Desserializar e persistir resultado
        // TODO: Atualizar AnalysisJobEntity
        // TODO: Gerar relatório
        // TODO Chamar o notification service quando terminar
    }
}
