package io.github.eduardosantiag0.kaizan_companion.services.enums;

public enum ECommands {
    STUDY ("/study"),
    COLLECTION("/collection"),
    LATEST("/latest"),
    ADD_MY_OGS("add-my-ogs");

    private final String name;

    ECommands(String s) {
        this.name = s;
    }

    public String getName() {
        return this.name;
    }
}
