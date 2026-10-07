package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class AnalysisListener {
    @RabbitListener(queues = "analysis-queue")
    public void listen(@Payload AnalysisCreatedEvent dto) {
        //! Desacoplada do KataGo
       //todo Criar varios workers
        //todo Enviar dto para os workers
    }
}
