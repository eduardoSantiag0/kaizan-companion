package io.github.eduardosantiag0.kaizan_companion.features.analysis;

import com.rabbitmq.client.Channel;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.errors.KatagoErrorMessage;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import io.github.eduardosantiag0.kaizan_companion.utils.MessageFormatter;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
    private final NotificationService notificationService;

    public RunPodClient(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Bulkhead(name = "runpod-bulkhead")
    @CircuitBreaker(name = "runpod", fallbackMethod = "fallbackMethod")
    public KatagoErrorMessage sendEvent(AnalysisCreatedEvent event, String postUrl, Channel channel, long deliveryTag) throws IOException {
        Map<String, AnalysisCreatedEvent> request = Map.of("input", event);

        return rest.postForObject(
                postUrl,
                request,
                KatagoErrorMessage.class
        );
    }

    private KatagoErrorMessage fallbackMethod(AnalysisCreatedEvent event, String postUrl, Channel channel, long deliveryTag, Exception e) throws IOException {
        UUID correlationId = event.analysisDTO().correlationId();

        if (e instanceof BulkheadFullException) {
            //TODO Cache?
            log.warn("Bulkhead cheio para o job {}. Reenfileirando para tentar mais tarde.", correlationId);
            channel.basicNack(deliveryTag, false, true);
            return null;
        }

        if (e instanceof CallNotPermittedException) {
            // Circuito aberto
            log.warn("Circuit breaker do RunPod aberto, não chamado para {}", correlationId);
            channel.basicNack(deliveryTag, false, true);
            notificationService.sendErrorMessage(new ReplyWithText(event.analysisDTO().chatId(),
                    MessageFormatter.systemOffline()));
            return null;
        }

        if (e instanceof HttpStatusCodeException httpEx) {
            var status = httpEx.getStatusCode();
            boolean shouldRetry = status.is5xxServerError() || status == HttpStatus.TOO_MANY_REQUESTS;

            log.error("RunPod respondeu {} para {}: {}", status, correlationId, httpEx.getMessage());
            channel.basicNack(deliveryTag, false, shouldRetry);
            return null;
        }

        if (e instanceof RestClientException) {
            // Falha de rede/timeout falando com o RunPod (ex: RunPod offline) — transitória
            log.error("Falha de comunicação com o RunPod para {}", correlationId, e);
            channel.basicNack(deliveryTag, false, true);
            return null;
        }

        log.error("Falha inesperada na análise {}", correlationId, e);
        // Mandar para DLQ: erro desconhecido, não arriscar loop de reprocessamento
        channel.basicNack(deliveryTag, false, false);
        return null;
    }
}

//- Processamento concluído = channel.basicAck(deliveryTag, false);
//- Falha temporária, tentar novamente = basicNack(tag, false, true)
//- Falha definitiva, não tentar novamente = basicNack(tag, false, false) -> DLQ
