package io.github.eduardosantiag0.kaizan_companion.infra.security;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CircuitBreakerQueueController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RabbitListenerEndpointRegistry rabbitRegistry;

    public CircuitBreakerQueueController(CircuitBreakerRegistry circuitBreakerRegistry,
                                         RabbitListenerEndpointRegistry rabbitRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.rabbitRegistry = rabbitRegistry;
    }

    @PostConstruct
    public void registerListeners() {
        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("runpod");

        breaker.getEventPublisher().onStateTransition(event -> {
            var transition = event.getStateTransition();
            log.info("Mudança de estado do Circuit Breaker '{}': de {} para {}",
                    event.getCircuitBreakerName(), transition.getFromState(), transition.getToState());

            AbstractMessageListenerContainer container =
                    (AbstractMessageListenerContainer) rabbitRegistry.getListenerContainer("analysisListenerContainer");

            if (container == null) {
                log.error("Container da fila 'analysisListenerContainer' não encontrado!");
                return;
            }

            switch (transition.getToState()) {
                case OPEN -> {
                    log.warn(" CIRCUITO ABERTO: Pausando consumo da fila de análise ");
                    container.stop();
                }
                case HALF_OPEN -> {
                    // Quando estiver Half-Open, aceitar novas requisições de teste
                    log.info(" CIRCUITO HALF-OPEN: Retomando consumo para teste ");
                    container.start();
                }
                case CLOSED -> {
                    log.info(" CIRCUITO FECHADO: Sistema normalizado, fila ativa ");
                    if (!container.isRunning()) {
                        container.start();
                    }
                }
            }
        });
    }
}
