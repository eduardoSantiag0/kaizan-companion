package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.messaging.RabbitMqUnavailableException;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.sgf.EmptySgfException;
import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging.AnalysisProducer;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.ICommandHandler;
import io.github.eduardosantiag0.kaizan_companion.features.sources.SourceContextStrategy;
import io.github.eduardosantiag0.kaizan_companion.features.sources.contracts.ISourceStrategy;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import io.github.eduardosantiag0.sgf.parser.SgfParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static io.github.eduardosantiag0.kaizan_companion.utils.GeneralValidator.isValidURL;
@Service
@Slf4j
public class CollectionCommandHandler implements ICommandHandler {

    private final TelegramChatRepository telegramChatRepository;
    private final SourceContextStrategy sourceContextStrategy;
    private final AnalysisProducer analysisProducer;
    private final NotificationService notificationService;

    // Task assincrona com URL original para debug
    private record GameTask(
            String url,
            Future<?> future
    ) {}

    public CollectionCommandHandler(
            TelegramChatRepository telegramChatRepository,
            SourceContextStrategy sourceContextStrategy,
            AnalysisProducer analysisProducer,
            NotificationService notificationService
    ) {
        this.telegramChatRepository = telegramChatRepository;
        this.sourceContextStrategy = sourceContextStrategy;
        this.analysisProducer = analysisProducer;
        this.notificationService = notificationService;
    }

    @Override
    public ECommands getCommand() {
        return ECommands.COLLECTION;
    }

    @Override
    public void executeCommand(Update update)
            throws TelegramApiException, IOException {

        String[] fullMessage =
                update.getMessage().getText().split("\\s+");

        final List<String> gamesRequested = List.of(
                Arrays.copyOfRange(
                        fullMessage,
                        1,
                        fullMessage.length
                )
        );


        Long chatId = update.getMessage().getChatId();

        var chat = update.getMessage().getChat();

        if (gamesRequested.size() > 5) {
            notificationService.sendErrorMessage(new ReplyWithText(
                    chatId, "You can only request 5 games with /collections"
            ));
            return;
        }

        TelegramChatEntity entity = telegramChatRepository
                .findById(chatId)
                .orElseGet(() -> telegramChatRepository.save(
                        new TelegramChatEntity(
                                chatId,
                                chat.getFirstName(),
                                chat.getUserName(),
                                chat.getType(),
                                true
                        )
                ));

        List<GameTask> resultsPerTask = new ArrayList<>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (String url : gamesRequested) {
                Future<?> future = executor.submit(() -> {

                    if (!isValidURL(url)) {
                        throw new IllegalArgumentException(
                                "Invalid URL: " + url
                        );
                    }

                    ISourceStrategy source =
                            sourceContextStrategy.setStrategy(url);

                    String id = source.extractId(url);

                    ByteArrayResource fileData =
                            source.downloadGameById(id);

                    if (fileData == null) {
                        throw new EmptySgfException(
                                "File data is null"
                        );
                    }

                    try {
                        analysisProducer.execute(chatId, fileData);

                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);

                    } catch (AmqpConnectException e) {
                        throw new RabbitMqUnavailableException(
                                "RabbitMQ is unavailable",
                                e
                        );
                    }

                });

                resultsPerTask.add(new GameTask(url, future));
            }
        }

        List<Throwable> errors = new ArrayList<>();
        List<String> failedUrls = new ArrayList<>();

        for (GameTask task : resultsPerTask) {
            try {
                task.future().get();

            } catch (ExecutionException e) {
                failedUrls.add(task.url());
                RuntimeException error = new RuntimeException(
                        "Failed to process URL: " + task.url(),
                        e.getCause()
                );
                errors.add(error);
                log.error(
                        "Failed to process game: {}",
                        task.url(),
                        e.getCause()
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted", e);
            }
        }
        int successfulTasks = resultsPerTask.size() - errors.size();
        if (successfulTasks > 0) {
            entity.setWaitingForAnalysis(true);
            telegramChatRepository.save(entity);
        }

        log.info(
                "Collection completed: {} successful, {} failed",
                successfulTasks,
                errors.size()
        );

        // Exceção agregada
        if (!errors.isEmpty()) {

            RuntimeException agg = new RuntimeException(
                    "Failed to process " + errors.size()
                            + " games:\n"
                            + String.join("\n", failedUrls)
            );

            errors.forEach(agg::addSuppressed);

            throw agg;
        }
    }
}


// Executor recebe uma tarefa e decidir como executá-la.

// Runnable representa uma tarefa sem valor de retorno

// Callable<?> Callable representa uma tarefa que pode retornar um valor e lançar exceções verificadas

// ExecutorService = Executor + funcionalidades para submeter tarefas, recuperar resultados, cancelar operações e administrar o ciclo de vida do executor.
    // execute: fire and forget
    // submit: retorna Future

// Future<T> representa um resultado que pode ainda não estar disponível.
    // Para várias etapas, a thread coordenadora fica esperando cada etapa.
// CompletableFuture: construindo fluxos assíncronos
    // supplyAsync() — quando existe um resultado
    // runAsync() — quando não existe resultado
// ExecutorCompletionService
    // processar resultados na ordem em que ficam prontos.

// ForkJoinPool: paralelismo para dividir e conquistar
// ConcurrentHashMap: Permite leituras e atualizações concorrentes sem exigir que você proteja cada operação com um lock externo.
