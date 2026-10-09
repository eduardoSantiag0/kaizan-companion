package io.github.eduardosantiag0.kaizan_companion.features.analysis.katago;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.AnalysisAggregator;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.dtos.KatagoResponse;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.runpod.dtos.RunPodResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kaizan/api/v1/katago")
public class KatagoController {

    private final AnalysisAggregator aggregator;

    public KatagoController(AnalysisAggregator aggregator) {
        this.aggregator = aggregator;
    }

    @PostMapping
    public ResponseEntity<Void>  receiveAnalysis(@RequestBody RunPodResponse response) {
        aggregator.handleResponse(response.output());
        return ResponseEntity.ok().build();

    }
}
