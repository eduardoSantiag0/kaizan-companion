package io.github.eduardosantiag0.kaizan_companion.support;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public final class TelegramFixtures {

    private TelegramFixtures() {
    }

    public static Update textUpdate(Long chatId, String text) {
        Chat chat = new Chat(chatId, "private");
        chat.setFirstName("Tester");
        chat.setUserName("tester");

        Message message = new Message();
        message.setMessageId(1);
        message.setChat(chat);
        message.setText(text);

        Update update = new Update();
        update.setMessage(message);
        return update;
    }

    public static Update withoutMessage() {
        return new Update();
    }
}
