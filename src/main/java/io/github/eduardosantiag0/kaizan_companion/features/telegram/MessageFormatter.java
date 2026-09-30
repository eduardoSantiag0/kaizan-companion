package io.github.eduardosantiag0.kaizan_companion.features.telegram;

import io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos.PlayerInfo;

public class MessageFormatter {

    public static String formatGameMessage(String fileName) {
        return "The analysis for your game was completed: " + fileName;
    }

    public static String formatOgsAcccountLinked(PlayerInfo dto) {
        return "Account linked: " + dto.username() + "\nNow you can track your studies more easily!";

    }
}
