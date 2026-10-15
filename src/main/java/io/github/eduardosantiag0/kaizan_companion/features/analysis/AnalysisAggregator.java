package io.github.eduardosantiag0.kaizan_companion.features.analysis;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.JobStatus;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.runpod.dtos.RunPodResponse;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class AnalysisAggregator {
    private final NotificationService notificationService;
    private final TelegramChatRepository chatRepository;
    private final AnalysisJobRepository jobRepository;

    public AnalysisAggregator(NotificationService notificationService, TelegramChatRepository chatRepository, AnalysisJobRepository jobRepository) {
        this.notificationService = notificationService;
        this.chatRepository = chatRepository;
        this.jobRepository = jobRepository;
    }

    public void handleResponse(RunPodResponse response) {

        AnalysisJobEntity jobEntity = jobRepository.findByCorrelationId(response.output().correlationId()).orElseThrow(() -> new RuntimeException("Job nao encontrado"));
        jobEntity.setJobStatus(JobStatus.COMPLETED);
        // TODO: Desserializar e persistir resultado
//        var fileData = response.output().criticalPoints();

//        TelegramChatEntity entity = chatRepository.findById(response.chatId())
//                .orElseThrow(() -> new RuntimeException("Chat not found"));

        System.out.println("CHEGOU: " +  response.toString());


        // TODO: Atualizar AnalysisJobEntity

        // TODO: Gerar relatório
        // TODO Chamar o notification service quando terminar

//        notificationService.sendMessageToChat(
//                new ReplyWithFile(response.chatId(), fileData.getContentAsByteArray(),
//                        MessageFormatter.formatGameMessage(fileData.getContentAsByteArray())
//                )
//        );
    }
}
