package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.CommandHandler;
import io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos.OgsPlayerDTO;
import io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos.OgsSearchByUsernameDTO;
import io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos.PlayerInfo;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.MessageFormatter;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.dto.replies.ReplyWithFile;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.dto.replies.ReplyWithText;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class AddOgsCommandHandler implements CommandHandler {

    private final RestTemplate rest = new RestTemplate();
    private final TelegramChatRepository telegramChatRepository;
    private final NotificationService notificationService;

    public AddOgsCommandHandler(TelegramChatRepository telegramChatRepository, NotificationService notificationService) {
        this.telegramChatRepository = telegramChatRepository;
        this.notificationService = notificationService;
    }

    @Override
    public ECommands getCommand() {
        return ECommands.ADD_MY_OGS;
    }

    private String buidldEndpoint(String username) {
        return "https://online-go.com/api/v1/players/?username="+username;
//        https://online-go.com/api/v1/players/?username=SuperDU69

    }

    @Override
    public void executeCommand(Update update) throws TelegramApiException {
        String[] fullMessage = update.getMessage().getText().split("\\s+");;
        String username = fullMessage[1];
        String endpoint = buidldEndpoint(username);

        try {

            var dto = rest.getForObject(endpoint, OgsSearchByUsernameDTO.class);

            if (dto.count() == 0) {
                throw new RuntimeException("Username not found");
            }

            if (dto.results().size() > 1) {
                throw new RuntimeException("More than one player was found");
            }

            PlayerInfo playerInfo = dto.results().get(0);

            Long chatId = update.getMessage().getChatId();

            TelegramChatEntity entity = telegramChatRepository.findById(chatId)
                            .orElseThrow(() -> new RuntimeException("Chat not found"));

            entity.setOgsAccountName(playerInfo.username());

            notificationService.sendMessageToChat(new ReplyWithText(
                    chatId, MessageFormatter.formatOgsAcccountLinked(playerInfo)
            ));

        } catch (RestClientException e) {
            System.out.println("Error fetching data: " + e.getMessage());
        }

    }
}

//1658916
