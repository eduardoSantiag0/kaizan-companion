package io.github.eduardosantiag0.kaizan_companion.exceptions.telegram;

import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;

public class CommandMisuseException extends RuntimeException {

    public CommandMisuseException(ECommands command) {
        super(usageFor(command));
    }

    private static String usageFor(ECommands command) {
        return switch (command) {
            case START -> "Usage: /start";
            case STUDY -> "Usage: /study <link>";
            case COLLECTION -> "Usage: /collection <link1> <link2> ...";
            case LAST_GAME -> "Usage: /last_game";
            case ADD_ACCOUNT_OGS -> "Usage: /add_account_ogs <username>";
            case ADD_ACCOUNT_DRAGON -> "Usage: /add_account_dragon <link>";
            case ADD_EMAIL -> "Usage: /add_email <email>";
            case RESIGN -> "Usage: /resign";
            case MY_PROFILE -> "Usage: /my_profile";
            case UNLINK -> "Usage: /unlink email|ogs";
            case QUEUE -> "Usage: /queue";
            case RANDOM -> "Usage: /random";
            case ADD_GOOGLE_DRIVE -> "Usage: /add_google_drive";
            case HISTORY -> "Usage: /history";
        };
    }
}
