package io.github.eduardosantiag0.kaizan_companion.domain.models;

public enum ECommands {
    STUDY ("/study"),
    COLLECTION("/collection"),
    LATEST("/latest"),
    ADD_MY_OGS("add-my-ogs");

    private final String name;

    ECommands(String s) {
        this.name = s;
    }

}
