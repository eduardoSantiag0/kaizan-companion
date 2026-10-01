package io.github.eduardosantiag0.kaizan_companion.features.telegram;

import io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos.MinimalPlayer;
import io.github.eduardosantiag0.sgf.model.SgfCollection;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import io.github.eduardosantiag0.sgf.model.SgfNode;
import io.github.eduardosantiag0.sgf.parser.SgfParser;

public class MessageFormatter {

    public static String formatGameMessage(String fileName) {
        return "The analysis for your game was completed: " + fileName;
    }

    public static String formatGameMessage(byte[] file) {

        SgfCollection collection = new SgfParser().parse(file);
        SgfGameTree game = collection.game(0);
        SgfNode root = game.root();


        String black = root.value("PB").orElse("desconhecido");
        String white = root.value("PW").orElse("desconhecido");
        String players = "Jogadores: " + black + " vs " + white;

        return "The analysis for your game was completed: " + players;

    }

    public static String formatOgsAcccountLinked(MinimalPlayer dto) {
        return "Account linked: " + dto.username() + "\nNow you can track your studies more easily!";
    }

}
