package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.AnalysisAggregator;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.dtos.KatagoQuery;
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
            String handlerEndpoint,
            AnalysisAggregator aggregator) {
        POST_URL = handlerEndpoint;
        this.aggregator = aggregator;
    }

    @RabbitListener(queues = "${rabbitmq.exchange.analysis.queue}")
    public void listen(@Payload AnalysisCreatedEvent event) {

        Map<String, AnalysisCreatedEvent> request = Map.of(
                "input", event
        );
        log.debug("Enviado: {}",request);

        String  response = null;
        response = rest.postForObject(
                POST_URL,
                request,
                String.class
        );

        log.debug("Resposta KataGo: {}", response);
//        "error":"Must be an array of pairs of the form: [\"b\" or \"w\", GTP board vertex]"}
//        aggregator.handleResponse(response);

    }
}
