package io.github.eduardosantiag0.kaizan_companion.features.analysis;

import com.rabbitmq.client.Channel;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.errors.KatagoErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
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

            return rest.postForObject(
                    postUrl,
                    request,
                    KatagoErrorMessage.class
            );
        }
        catch (HttpStatusCodeException e) {
            var status = e.getStatusCode();
            boolean shouldRetry = status.is5xxServerError() || status == HttpStatus.TOO_MANY_REQUESTS;

            log.error("RunPod respondeu {} para {}: {}", status, correlationId, e.getMessage());
            channel.basicNack(deliveryTag, false, shouldRetry);

        } catch (RestClientException e) {
            // Falha de rede/timeout falando com o RunPod (ex: RunPod offline) — transitória
            log.error("Falha de comunicação com o RunPod para {}", correlationId, e);
            channel.basicNack(deliveryTag, false, true);
        }

        catch (RuntimeException e) {
            log.error("Falha inesperada na análise {}", correlationId, e);
            // Mandar para DLQ: erro desconhecido, não arriscar loop de reprocessamento
            channel.basicNack(deliveryTag, false, false);
        }

        return null;
    }

}

//- Processamento concluído = channel.basicAck(deliveryTag, false);
//- Falha temporária, tentar novamente = basicNack(tag, false, true)
//- Falha definitiva, não tentar novamente = basicNack(tag, false, false) -> DLQ
