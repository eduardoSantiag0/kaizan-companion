package io.github.eduardosantiag0.kaizan_companion.infra.security.config.runpod;

import io.github.resilience4j.common.bulkhead.configuration.BulkheadConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RunPodBulkheadConfiguration {

    private static final int MAX_CONCURRENT_CALLS = 3;
    private static final int MAX_WAIT_DURATION_SECONDS = 20;

    @Bean
    public BulkheadConfigCustomizer  runpodBulkheadCustom() {
        return BulkheadConfigCustomizer.of("runpod-bulkhead", builder -> builder
                .maxConcurrentCalls(MAX_CONCURRENT_CALLS)
                .maxWaitDuration(Duration.ofSeconds(MAX_WAIT_DURATION_SECONDS))
        );
    }
}
