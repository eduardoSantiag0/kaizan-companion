package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import io.github.eduardosantiag0.kaizan_companion.domain.models.GameHistoryDto;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.GameAnalysisRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.GamePhase;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.ICommandHandler;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithFile;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import io.github.eduardosantiag0.kaizan_companion.utils.MessageFormatter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
public class HistoryCommandHandler implements ICommandHandler {

    private final TelegramChatRepository telegramChatRepository;
    private final GameAnalysisRepository gameAnalysisRepository;
    private final NotificationService notificationService;

    public HistoryCommandHandler(TelegramChatRepository telegramChatRepository, GameAnalysisRepository gameAnalysisRepository, NotificationService notificationService) {
        this.telegramChatRepository = telegramChatRepository;
        this.gameAnalysisRepository = gameAnalysisRepository;
        this.notificationService = notificationService;
    }

    @Override
    public ECommands getCommand() {
        return ECommands.HISTORY;
    }

    @Override
    public void executeCommand(Update update) throws TelegramApiException, IOException {

        Long chatId = update.getMessage().getChatId();
        var chat = update.getMessage().getChat();

        TelegramChatEntity chatEntity = telegramChatRepository
                .findById(chatId)
                .orElseGet(() -> new TelegramChatEntity(
                        chatId,
                        chat.getFirstName(),
                        chat.getUserName(),
                        chat.getType(),
                        false
                ));

        List<GameHistoryDto> gamesSaved = gameAnalysisRepository.findHistoryByChat(chatEntity);

        if (gamesSaved.isEmpty()) {
            notificationService.sendErrorMessage(new ReplyWithText(chatId, MessageFormatter.noHistory()));
            return;
        }

        for (GameHistoryDto game : gamesSaved) {
            sendPhaseIfPresent(chatId, game.openingAnalysed(), GamePhase.opening);
            sendPhaseIfPresent(chatId, game.middlegameAnalysed(), GamePhase.middlegame);
            sendPhaseIfPresent(chatId, game.endgameAnalysed(), GamePhase.endgame);
        }
    }

    private void sendPhaseIfPresent(Long chatId, String sgf, GamePhase phase) {
        if (sgf == null) {
            return;
        }

        byte[] data = sgf.getBytes(StandardCharsets.UTF_8);

        notificationService.sendMessageToChat(new ReplyWithFile(
                chatId, data, MessageFormatter.requestCompleted(data), phase, UUID.randomUUID()
        ));
    }
}
