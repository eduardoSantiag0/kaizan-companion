package io.github.eduardosantiag0.kaizan_companion.domain.models;

public enum ECommands {
    STUDY ("/study"),
    COLLECTION("/collection"),
    LAST_GAME("/last_game"),
    ADD_MY_OGS("/add-my-ogs"),
    ADD_EMAIL("/add-email"),
    RESIGN("/resign"),
    MY_PROFILE("/my_profile"),
    UNLINK("/unlink"),
    QUEUE("/queue"),
    RANDOM("/random");

    private final String name;

    ECommands(String s) {
        this.name = s;
    }

}
