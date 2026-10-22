package io.github.eduardosantiag0.kaizan_companion.utils;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.GamePhase;
import io.github.eduardosantiag0.sgf.model.SgfCollection;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import io.github.eduardosantiag0.sgf.model.SgfNode;
import io.github.eduardosantiag0.sgf.parser.SgfParser;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class FilenamePattern {
    public static String resolveFileName (byte[] file, GamePhase phase) {
        SgfCollection collection = new SgfParser().parse(file);
        SgfGameTree game = collection.game(0);
        SgfNode root = game.root();


        String black = root.value("PB").orElse("desconhecido");
        String white = root.value("PW").orElse("desconhecido");
        return black + white + "_" + phase.name().toUpperCase() + "_" + UUID.randomUUID() + ".sgf";
    }

}
