package io.github.eduardosantiag0.kaizan_companion.domain.models;

public enum ECommands {
    START ("/start"),
    STUDY ("/study"),
    COLLECTION("/collection"),
    LAST_GAME("/last_game"),
    ADD_ACCOUNT_OGS("/add_account_ogs"),
    ADD_ACCOUNT_DRAGON("/add_account_dragon"),
    ADD_EMAIL("/add_email"),
    RESIGN("/resign"),
    MY_PROFILE("/my_profile"),
    UNLINK("/unlink"),
    QUEUE("/queue"),
    RANDOM("/random"),
    ADD_GOOGLE_DRIVE("/add_google_drive"),
    HISTORY("/history");

    private final String command;

    ECommands(String s) {
        this.command = s;
    }

    public String getCommand() {
        return command;
    }

}
