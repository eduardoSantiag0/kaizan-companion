package io.github.eduardosantiag0.kaizan_companion.domain.models;

public enum ECommands {
    STUDY ("/study"),
    COLLECTION("/collection"),
    LAST_GAME("/last_game"),
    ADD_ACCOUNT_OGS("/add-account-ogs"),
    ADD_ACCOUNT_DRAGON("/add-account-dragon"),
    ADD_EMAIL("/add-email"),
    RESIGN("/resign"),
    MY_PROFILE("/my_profile"),
    UNLINK("/unlink"),
    QUEUE("/queue"),
    RANDOM("/random"),
    ADD_GOOGLE_DRIVE("/add-google-drive");

    private final String name;

    ECommands(String s) {
        this.name = s;
    }

}
