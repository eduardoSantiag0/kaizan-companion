package io.github.eduardosantiag0.kaizan_companion.exceptions.telegram;

import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;

public class CommandMisuseException extends RuntimeException {

    public CommandMisuseException(ECommands command) {
        super(usageFor(command));
    }

    private static String usageFor(ECommands command) {
        return switch (command) {
            case STUDY -> "Usage: /study <link>";
            case COLLECTION -> "Usage: /collection <link1> <link2> ...";
            case LAST_GAME -> "Usage: /last_game";
            case ADD_ACCOUNT_OGS -> "Usage: /add-account-ogs <username>";
            case ADD_ACCOUNT_DRAGON -> "Usage: /add-account-dragon <link>";
            case ADD_EMAIL -> "Usage: /add-email <email>";
            case RESIGN -> "Usage: /resign";
            case MY_PROFILE -> "Usage: /my-profile";
            case UNLINK -> "Usage: /unlink email|ogs";
            case QUEUE -> "Usage: /queue";
            case RANDOM -> "Usage: /random";
            case ADD_GOOGLE_DRIVE -> "Usage: /add-google-drive";
        };
    }
}
