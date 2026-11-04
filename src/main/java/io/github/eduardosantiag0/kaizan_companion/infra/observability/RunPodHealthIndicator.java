package io.github.eduardosantiag0.kaizan_companion.infra.observability;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@ConditionalOnEnabledHealthIndicator("runpod")
@Component("runpodHealthIndicator")
public class RunPodHealthIndicator implements HealthIndicator {

    @Value("${katago.runpod.status.health.url}")
    private String RUNPOD_STATUS_URL;

    @Override
    public Health health() {
        if (isExternalApiRunning()) {
            return Health.up().withDetail("Ext API1", "AVAILABLE").build();
        } else {
            return Health.outOfService().withDetail("Ext API1", "OUT-OF-SERVICE").build();
        }
    }

    private boolean isExternalApiRunning() {
        return true;
    }
}
