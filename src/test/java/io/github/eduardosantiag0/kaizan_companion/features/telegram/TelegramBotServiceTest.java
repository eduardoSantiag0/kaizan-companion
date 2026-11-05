package io.github.eduardosantiag0.kaizan_companion.features.telegram;

import io.github.eduardosantiag0.kaizan_companion.features.handlers.commands.*;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.support.TelegramFixtures;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class TelegramBotServiceTest {

    private TelegramBotService newService(StudyCommandHandler studyCommandHandler,
                                           NotificationService notificationService) {
        return new TelegramBotService(
                "KaizanBot",
                "dummy-token",
                studyCommandHandler,
                mock(TelegramClient.class),
                mock(AddOgsCommandHandler.class),
                mock(LastGameCommandHandler.class),
                notificationService,
                mock(ResignCommandHandler.class),
                mock(CollectionCommandHandler.class),
                mock(RandomCommandHandler.class),
                mock(QueueCommandHandler.class),
                mock(AddEmailCommandHandler.class),
                mock(MyProfileCommandHandler.class),
                mock(UnlinkCommandHandler.class),
                mock(AddDgsCommandHandler.class),
                mock(AddGoogleDriveCommandHandler.class)
        );
    }

    @Test
    void consume_whenAHandlerThrowsUnexpectedly_mustStillReplyToTheChat() throws Exception {
        NotificationService notificationService = mock(NotificationService.class);
        StudyCommandHandler studyCommandHandler = mock(StudyCommandHandler.class);
        doThrow(new ArrayIndexOutOfBoundsException("1")).when(studyCommandHandler).executeCommand(any());

        TelegramBotService service = newService(studyCommandHandler, notificationService);
        var update = TelegramFixtures.textUpdate(1L, "/study");

        assertDoesNotThrow(() -> service.consume(update));
        verify(notificationService).sendErrorMessage(any());
    }

    @Test
    void consume_whenUpdateHasNoMessage_mustNotThrow_andMustNotAttemptToReply() {
        // Regression test: Telegram also sends updates without a Message (edited messages,
        // channel posts, group membership changes, ...). update.getMessage() is null for
        // those, so handle()/consume() used to NPE instead of just ignoring the update.
        NotificationService notificationService = mock(NotificationService.class);
        StudyCommandHandler studyCommandHandler = mock(StudyCommandHandler.class);

        TelegramBotService service = newService(studyCommandHandler, notificationService);
        var update = TelegramFixtures.withoutMessage();

        assertDoesNotThrow(() -> service.consume(update));
        verifyNoInteractions(notificationService);
    }

    @Test
    void consume_whenMessageHasNoText_mustNotThrow_andMustNotAttemptToReply() {
        // A sticker, photo or voice message without a caption has a Message but a null text.
        NotificationService notificationService = mock(NotificationService.class);
        StudyCommandHandler studyCommandHandler = mock(StudyCommandHandler.class);

        TelegramBotService service = newService(studyCommandHandler, notificationService);
        var update = TelegramFixtures.textUpdate(1L, null);

        assertDoesNotThrow(() -> service.consume(update));
        verifyNoInteractions(notificationService);
    }
}
