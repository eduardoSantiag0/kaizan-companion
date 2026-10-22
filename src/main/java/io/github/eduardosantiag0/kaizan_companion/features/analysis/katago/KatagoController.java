package io.github.eduardosantiag0.kaizan_companion.features.analysis.katago;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.AnalysisAggregator;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.runpod.RunPodResponse;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/kaizan/api/v1/katago")
public class KatagoController {

    private final AnalysisAggregator aggregator;

    public KatagoController(AnalysisAggregator aggregator) {
        this.aggregator = aggregator;
    }

    @PostMapping
    public ResponseEntity<Void>  receiveAnalysis(@RequestBody RunPodResponse response) throws IOException, MessagingException {
        aggregator.handleResponse(response);
        return ResponseEntity.ok().build();

    }
}
