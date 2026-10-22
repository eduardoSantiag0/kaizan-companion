package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging.AnalysisProducer;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.CommandHandler;
import io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.OgsGameDownloader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class RandomCommandHandler implements CommandHandler {

    private final OgsGameDownloader ogsGameDownloader;
    private final TelegramChatRepository telegramChatRepository;
    private final AnalysisProducer analysisProducer;

    private static final Map<Short, String> RANDOM_GAMES = Map.ofEntries(
            Map.entry((short) 1, "86597702"),
            Map.entry((short) 2, "88507287"),
            Map.entry((short) 3, "9591352"),
            Map.entry((short) 4, "8116700"),
            Map.entry((short) 5, "8115970"),
            Map.entry((short) 6, "89394914"),
            Map.entry((short) 7, "8145209"),
            Map.entry((short) 8, "9682422"),
            Map.entry((short) 9, "88193600"),
            Map.entry((short) 10, "88169372"),
            Map.entry((short) 11, "86327683"),
            Map.entry((short) 12, "69611592"),
            Map.entry((short) 13, "12984068"),
            Map.entry((short) 14, "9196762"),
            Map.entry((short) 15, "17744867"),
            Map.entry((short) 16, "2822109"),
            Map.entry((short) 17, "14212568"),
            Map.entry((short) 18, "8653113"),
            Map.entry((short) 19, "6502571"),
            Map.entry((short) 20, "21756678"),
            Map.entry((short) 21, "33551102"),
            Map.entry((short) 22, "89513426"),
            Map.entry((short) 23, "9735129"),
            Map.entry((short) 24, "6825760"),
            Map.entry((short) 25, "14398898"),
            Map.entry((short) 26, "19618095"),
            Map.entry((short) 27, "35517450"),
            Map.entry((short) 28, "35215830"),
            Map.entry((short) 29, "5219047"),
            Map.entry((short) 30, "71079776"),
            Map.entry((short) 31, "71079777"),
            Map.entry((short) 32, "40102110"),
            Map.entry((short) 33, "58208576"),
            Map.entry((short) 34, "90681547"),
            Map.entry((short) 35, "87210497"),
            Map.entry((short) 36, "13092045"),
            Map.entry((short) 37, "87436155"),
            Map.entry((short) 38, "65026693"),
            Map.entry((short) 39, "14751624"),
            Map.entry((short) 40, "54276980"),
            Map.entry((short) 41, "54285727"),
            Map.entry((short) 42, "46889359"),
            Map.entry((short) 43, "12966503"),
            Map.entry((short) 44, "40617904"),
            Map.entry((short) 45, "24471878"),
            Map.entry((short) 46, "84595802"),
            Map.entry((short) 47, "84595801"),
            Map.entry((short) 48, "84595690"),
            Map.entry((short) 49, "84595803"),
            Map.entry((short) 50, "34535737")
    );

    public RandomCommandHandler(OgsGameDownloader ogsGameDownloader,TelegramChatRepository telegramChatRepository, AnalysisProducer analysisProducer) {
        this.ogsGameDownloader = ogsGameDownloader;
        this.telegramChatRepository = telegramChatRepository;
        this.analysisProducer = analysisProducer;
    }

    @Override
    public ECommands getCommand() {
        return ECommands.RANDOM;
    }

    @Override
    public void executeCommand(Update update) throws TelegramApiException, IOException {

        short rand = (short) ((short)(Math.random() * RANDOM_GAMES.size()) + 1);

        String id = RANDOM_GAMES.getOrDefault(rand, "86597702");

        ByteArrayResource fileData = ogsGameDownloader.downloadGameById(id);

        if (fileData == null) {
            log.error("file data is null");
            return;
        }


        Long chatId = update.getMessage().getChatId();
        var chat = update.getMessage().getChat();
        TelegramChatEntity entity = telegramChatRepository
                .findById(chatId)
                .orElseGet(() -> new TelegramChatEntity(
                        chatId,
                        chat.getFirstName(),
                        chat.getUserName(),
                        chat.getType(),
                        true
                ));

        entity.setWaitingForAnalysis(true);
        telegramChatRepository.save(entity);

        analysisProducer.execute(chatId, fileData);


    }
}
