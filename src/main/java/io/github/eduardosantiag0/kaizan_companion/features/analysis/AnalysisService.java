//package io.github.eduardosantiag0.kaizan_companion.features.analysis;
//
//import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
//import io.github.eduardosantiag0.kaizan_companion.domain.exception.CustomExceptionHandler;
//import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
//import io.github.eduardosantiag0.kaizan_companion.features.files.FileUploadService;
//import io.github.eduardosantiag0.kaizan_companion.features.telegram.ICommands;
//import io.github.eduardosantiag0.kaizan_companion.features.sources.usecases.DownloadGameByIdUseCase;
//import io.github.eduardosantiag0.kaizan_companion.features.telegram.MessageFormatter;
//import io.github.eduardosantiag0.kaizan_companion.features.telegram.TelegramBotService;
//import io.github.eduardosantiag0.kaizan_companion.features.telegram.dto.TelegramSendMessage;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//import org.telegram.telegrambots.meta.api.objects.Update;
//
//import java.net.MalformedURLException;
//import java.net.URISyntaxException;
//import java.net.URL;
//
//import static io.github.eduardosantiag0.kaizan_companion._shared.GeneralValidator.isValidURL;
//
//@Service
//public class AnalysisService implements ICommands {
//
//    private final DownloadGameByIdUseCase downloadGameByIdUseCase;
//    private final FileUploadService fileUploadService;
//    private final TelegramChatRepository telegramChatRepository;
//    private final TelegramBotService telegramService;
//
//    private final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);
//
//    public AnalysisService(DownloadGameByIdUseCase downloadGameByIdUseCase, FileUploadService fileUploadService, TelegramChatRepository telegramChatRepository, TelegramBotService telegramService) {
//        this.downloadGameByIdUseCase = downloadGameByIdUseCase;
//        this.fileUploadService = fileUploadService;
//        this.telegramChatRepository = telegramChatRepository;
//        this.telegramService = telegramService;
//    }
//
//
//    @Override
//    public void study(String url, Update update) {
//        if (!isValidURL(url)) {
//            return;
//        }
//        byte[]fileData =  downloadGameByIdUseCase.execute(url);
//
//        if (fileData == null) {
//            logger.error("file data is null");
//            return;
//        }
//
//        String fileName = fileUploadService.uploadFile(fileData);
//
//        Long chatId = update.getMessage().getChatId();
//        var chat = update.getMessage().getChat();
//        TelegramChatEntity entity = telegramChatRepository
//                .findByChatId(chatId)
//                .orElseGet(() -> new TelegramChatEntity(
//                        chatId,
//                        chat.getFirstName(),
//                        chat.getUserName(),
//                        chat.getType(),
//                        null,
//                        null,
//                        true
//                ));
//
//        entity.setWaitingForAnalysis(true);
//        telegramChatRepository.save(entity);
//
//        telegramService.sendMessageToChat(
//                new TelegramSendMessage(chatId, fileData,
//                        MessageFormatter.formatGameMessage(fileName)
//                )
//        );
//
//    }
//
//    @Override
//    public void collection() {
//
//    }
//
//    @Override
//    public void addEmail() {
//
//    }
//
//    @Override
//    public void addGoodleDrive() {
//
//    }
//
//    @Override
//    public void addMyProfile() {
//
//    }
//
//    @Override
//    public void latest() {
//
//    }
//}
