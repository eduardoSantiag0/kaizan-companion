package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.AnalysisAggregator;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.dtos.KatagoResponse;
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

    public AnalysisListener(
            @Value("${katago.post.url.sync}")
            String handlerEndpoint, AnalysisAggregator aggregator) {
        POST_URL = handlerEndpoint;
        this.aggregator = aggregator;
    }

    @RabbitListener(queues = "analysis-queue")
    public void listen(@Payload AnalysisCreatedEvent event) {

        log.debug("Enviado: {}",event.katagoQuery());

        var request = Map.of(
                "input", event.katagoQuery()
        );

        String  response = rest.postForObject(
                POST_URL,
                request,
                String.class
//                KatagoResponse.class
        );
        log.debug("Resposta KataGo: {}", response);

//        aggregator.handleResponse(response);

    }
}
