package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.AnalysisAggregator;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.JobStatus;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.errors.KatagoErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Slf4j
public class AnalysisListener {

    private final String POST_URL;
    private final RestTemplate rest = new RestTemplate();
    private final AnalysisAggregator aggregator;
    private final AnalysisJobRepository jobRepository;

    public AnalysisListener(
            @Value("${katago.post.url.sync}")
            String handlerEndpoint,
            AnalysisAggregator aggregator, AnalysisJobRepository jobRepository) {
        POST_URL = handlerEndpoint;
        this.aggregator = aggregator;
        this.jobRepository = jobRepository;
    }

    @RabbitListener(queues = "${rabbitmq.exchange.analysis.queue}")
    public void listen(@Payload AnalysisCreatedEvent event) {

        Map<String, AnalysisCreatedEvent> request = Map.of(
                "input", event
        );
        log.debug("Enviado: {}",request);

        KatagoErrorMessage response = rest.postForObject(
                POST_URL,
                request,
                KatagoErrorMessage .class
        );

        AnalysisJobEntity jobEntity = jobRepository.findByCorrelationId(event.analysisDTO().correlationId()).orElseThrow(() -> new RuntimeException("Job nao encontrado"));


        if (response != null && response.status().equals("FAILED")) {
            jobEntity.setJobStatus(JobStatus.FAILED);
            log.error("Resposta KataGo: {}", response);
            return;
        }


        jobEntity.setJobStatus(JobStatus.IN_QUEUE);

        log.debug("Resposta KataGo: {}", response);


    }
}
