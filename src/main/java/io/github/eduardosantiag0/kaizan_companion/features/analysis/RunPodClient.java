package io.github.eduardosantiag0.kaizan_companion.features.analysis;

import com.rabbitmq.client.Channel;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.analysis.AnalysisJobNotFoundException;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.errors.KatagoErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class RunPodClient {

    private final RestTemplate rest = new RestTemplate();

    public KatagoErrorMessage sendEvent(AnalysisCreatedEvent event, String postUrl, Channel channel, long deliveryTag) throws IOException {
        UUID correlationId = event.analysisDTO().correlationId();

        try {

            Map<String, AnalysisCreatedEvent> request =
                    Map.of("input", event);

            // Erro no Katago
            return rest.postForObject(
                    postUrl,
                    request,
                    KatagoErrorMessage.class
            );
        }
        catch (AnalysisJobNotFoundException e) {
            log.error("Job inexistente: {}", correlationId, e);

            // Falha definitiva -> DLQ, se configurada
            channel.basicNack(deliveryTag, false, false);

        } catch (RestClientException e) {
            log.error("Falha HTTP ao enviar análise {}", correlationId, e);

            // Falha temporaria, tentar novamente
            channel.basicNack(deliveryTag, false, true);

        } catch (RuntimeException e) {
            log.error("Falha inesperada na análise {}", correlationId, e);

            channel.basicNack(deliveryTag, false, true);
        }

        return null;
    }

}
