package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.UserInfoEntity;
import io.github.eduardosantiag0.kaizan_companion.exceptions.telegram.CommandMisuseException;
import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.UserInfoRepository;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.ICommandHandler;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import io.github.eduardosantiag0.kaizan_companion.features.sources.dgs.dtos.DgsPlayerDTO;
import io.github.eduardosantiag0.kaizan_companion.utils.GeneralValidator;
import io.github.eduardosantiag0.kaizan_companion.utils.MessageFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AddDgsCommandHandler implements ICommandHandler {

    private static final Pattern UID_PATTERN = Pattern.compile("uid=(\\d+)");
    private static final String DGS_HOST = "dragongoserver.net";

    private final String DGS_BASE_URL;

    private final TelegramChatRepository telegramChatRepository;
    private final NotificationService notificationService;
    private final UserInfoRepository userInfoRepository;
    private final RestTemplate rest = new RestTemplate();


    public AddDgsCommandHandler(
            @Value("${dgs.baseurl}")
            String dgsBaseUrl, TelegramChatRepository telegramChatRepository,
            NotificationService notificationService, UserInfoRepository userInfoRepository) {

        this.DGS_BASE_URL = dgsBaseUrl;
        this.telegramChatRepository = telegramChatRepository;
        this.notificationService = notificationService;
        this.userInfoRepository = userInfoRepository;
    }

    @Override
    public ECommands getCommand() {
        return ECommands.ADD_ACCOUNT_DRAGON;
    }

    private String extractUid(String profileUrl) {
        Matcher matcher = UID_PATTERN.matcher(profileUrl);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid DGS profile URL: " + profileUrl);
        }

        return matcher.group(1);
    }

    @Override
    public void executeCommand(Update update) throws TelegramApiException {
        String[] fullMessage = update.getMessage().getText().split("\\s+");

        if (fullMessage.length < 2) {
            notificationService.sendErrorMessage(new ReplyWithText(
                    update.getMessage().getChatId(), new CommandMisuseException(getCommand()).getMessage()));
            return;
        }

        String profileUrl = fullMessage[1];
        if (!GeneralValidator.validateHost(profileUrl, DGS_HOST)) {
            notificationService.sendErrorMessage(new ReplyWithText(update.getMessage().getChatId(),
                    "You must send a valid url of Dragon Go Server!\n"));
            return;
        }
        String uid = extractUid(profileUrl);

        log.debug("Linking DGS uid {}", uid);

        try {

            var dto = rest.getForObject(profileUrl, DgsPlayerDTO.class);

            log.debug(dto.toString());

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

            UserInfoEntity userInfo = new UserInfoEntity(entity);

            userInfo.setDgsAccountId(uid);
            userInfoRepository.save(userInfo);

            telegramChatRepository.save(entity);

            notificationService.sendMessageToChat(new ReplyWithText(
                    chatId, MessageFormatter.formatAccountLinked(dto.username(), "DGS")
            ));
        } catch (RestClientException e) {
            log.error("Failed to fetch DGS profile {}", profileUrl, e);
            notificationService.sendErrorMessage(new ReplyWithText(
                    update.getMessage().getChatId(), MessageFormatter.requestFailed()));
        }


    }
}
