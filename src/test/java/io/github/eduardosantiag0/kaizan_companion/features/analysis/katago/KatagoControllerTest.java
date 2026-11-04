package io.github.eduardosantiag0.kaizan_companion.features.analysis.katago;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.AnalysisAggregator;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.runpod.GameAnalysisResponse;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.runpod.RunPodResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


class KatagoControllerTest {

    @Test
    void receiveAnalysis_mustNotForwardAnUnauthenticatedForgedCallback() throws Exception {
        AnalysisAggregator aggregator = mock(AnalysisAggregator.class);
        KatagoController controller = new KatagoController(aggregator, "expected-secret");

        RunPodResponse forgedCallback = new RunPodResponse(
                "fake-runpod-id",
                new RunPodResponse.JobInformations(
                        123456789L,
                        UUID.randomUUID(),
                        "COMPLETED",
                        new GameAnalysisResponse("fake-game-id", 0, List.of())
                )
        );

        try {
            controller.receiveAnalysis(null, forgedCallback);
        } catch (Exception ignored) {
            // Rejecting by throwing is an acceptable fix too; only the dispatch matters here.
        }

        verify(aggregator, never()).handleResponse(forgedCallback);
    }
}
