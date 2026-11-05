package io.github.eduardosantiag0.kaizan_companion.features.analysis.katago;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.AnalysisAggregator;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.runpod.RunPodResponse;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/kaizan/api/v1/katago")
public class KatagoController {

    private final AnalysisAggregator aggregator;

    private static final String RUNPOD_SECRET_HEADER = "X-Kaizen-Secret";

    private final String RUNPOD_HEADER_SECRET;

    public KatagoController(AnalysisAggregator aggregator,
                            @Value("${katago.runpod.header-secret}")
                            String runpodHeaderSecret)
    {
        this.aggregator = aggregator;
        RUNPOD_HEADER_SECRET = runpodHeaderSecret;
    }

    @PostMapping
    public ResponseEntity<Void> receiveAnalysis(
            @RequestHeader(value = RUNPOD_SECRET_HEADER, required = false) String secret,
            @RequestBody RunPodResponse response) throws IOException, MessagingException
    {
        if (secret == null || !constantTimeEquals(secret, RUNPOD_HEADER_SECRET)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        aggregator.handleResponse(response);
        return ResponseEntity.ok().build();
    }

    private static boolean constantTimeEquals(String received, String expected) {
        return MessageDigest.isEqual(
                received.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8)
        );
    }
}
