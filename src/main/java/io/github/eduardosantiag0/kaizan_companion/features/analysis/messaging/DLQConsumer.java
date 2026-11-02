package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DLQConsumer {

    private String getReason(String reason) {
        if (reason == null) return "Desconhecido";
        return switch (reason) {
            case "expired" -> "TTL Expired";
            case "maxlen" -> "Queue at max";
            case "rejected" -> "Rejected by Consumer";
            default -> reason;
        };
    }

    @RabbitListener(queues = "${rabbitmq.queue.analysis.dlq}")
    public void consumeDL(AnalysisCreatedEvent payload, MessageHeaders headers) {

        log.debug("Headers: " + headers);

        if (headers.containsKey("x-death")) {
            List<?> xDeathList = (List<?>) headers.get("x-death");

            if (xDeathList != null && !xDeathList.isEmpty()) {
                Map<?, ?> lastError = (Map<?, ?>) xDeathList.get(0);

                String reason = lastError.get("reason") != null ? lastError.get("reason").toString() : "unknown";
                String originalQueue = lastError.get("queue") != null ? lastError.get("queue").toString() : "unknown";

                log.debug("Origin Queue: " + originalQueue);
                log.debug("Reason: " + getReason(reason));
            }
        } else {
            log.debug("Mensagem enviada para a DLQ (sem x-death).");
        }
    }

}



//Headers: {
//
//    amqp_receivedDeliveryMode=PERSISTENT,
//    amqp_receivedExchange=analysis.dlx,
//    amqp_deliveryTag=7,
//    amqp_consumerQueue=analysis.dql,
//    x-last-death-reason=rejected,
//    amqp_redelivered=false,
//    amqp_receivedRoutingKey=analysis.failed,
//    amqp_contentEncoding=UTF-8,
//    x-first-death-exchange=kaizen.exchange,
//    x-death=[{
//        reason=rejected, count=1, exchange=kaizen.exchange, time=Mon Nov 02 16:06:49 BRT 2026, routing-keys=[analysis.created], queue=analysis.queue
//        }],
//    x-first-death-reason=rejected,
//    x-first-death-queue=analysis.queue,
//    x-last-death-queue=analysis.queue,
//    amqp_retryCount=1,
//    id=a6406dbe-4c6d-d0ba-9b71-9b822ee6260f,
//    amqp_consumerTag=amq.ctag-wR1pEz-NzqVEX9PIO6dsaw,
//    amqp_lastInBatch=false, x-last-death-exchange=kaizen.exchange,
//    contentType=application/json,
//    __TypeId__=io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent,
//    timestamp=1793646409441
//
//}
//
//
