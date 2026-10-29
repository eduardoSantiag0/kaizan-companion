package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.ICommandHandler;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import io.github.eduardosantiag0.kaizan_companion.infra.auth.ticket.TicketService;
import io.github.eduardosantiag0.kaizan_companion.utils.MessageFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;


@Service
@Slf4j
public class AddGoogleDriveCommandHandler implements ICommandHandler {

    private final NotificationService notificationService;
    private final TicketService ticketService;

    public AddGoogleDriveCommandHandler(NotificationService notificationService, TicketService ticketService) {
        this.notificationService = notificationService;
        this.ticketService = ticketService;
    }

    @Override
    public ECommands getCommand() {
        return ECommands.ADD_GOOGLE_DRIVE;
    }

    @Override
    public void executeCommand(Update update) throws TelegramApiException, IOException {

        Long chatId = update.getMessage().getChatId();

        var url = ticketService.generateUrl(chatId);

        log.debug(url);

        notificationService.sendMessageToChat(new ReplyWithText(
                chatId, MessageFormatter.formatLinkGoogle(url)
        ));

    }
}
