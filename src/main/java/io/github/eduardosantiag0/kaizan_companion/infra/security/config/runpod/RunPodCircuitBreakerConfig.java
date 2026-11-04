package io.github.eduardosantiag0.kaizan_companion.infra.security.config.runpod;

import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

@Configuration
public class RunPodCircuitBreakerConfig {

    @Bean
    public CircuitBreakerConfigCustomizer runpodCircuitBreakerConfigCustomizer() {
        return CircuitBreakerConfigCustomizer.of("runpod", builder ->
                builder.recordException(RunPodCircuitBreakerConfig::isTransientFailure));
    }

    private static boolean isTransientFailure(Throwable e) {
        if (e instanceof HttpStatusCodeException httpEx) {
            var status = httpEx.getStatusCode();
            return status.is5xxServerError() || status == HttpStatus.TOO_MANY_REQUESTS;
        }
        return e instanceof RestClientException;
    }
}
