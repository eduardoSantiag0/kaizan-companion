package io.github.eduardosantiag0.kaizan_companion.utils;

import io.github.eduardosantiag0.kaizan_companion.domain.exception.sgf.SgfForbiddenFormat;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.GamePhase;
import io.github.eduardosantiag0.sgf.model.SgfCollection;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import io.github.eduardosantiag0.sgf.model.SgfNode;
import io.github.eduardosantiag0.sgf.parser.SgfParser;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class FilenamePattern {

    private final String[] forbiddenChars = {"..", "/", "\\"};

    public static String resolveFileName (byte[] file, GamePhase phase, UUID randomId) throws SgfForbiddenFormat {
        SgfCollection collection = new SgfParser().parse(file);
        SgfGameTree game = collection.game(0);
        SgfNode root = game.root();

        String black = root.value("PB").orElse("Black");
        String white = root.value("PW").orElse("White");

//        if (String.valueOf(forbiddenChars).contains(black) || String.valueOf(forbiddenChars).contains(white) ) {
//            throw new SgfForbiddenFormat("Sgf contains forbidden characters");
//        }

        for (String forbidden : forbiddenChars) {
            if (black.contains(forbidden) && !black.equals("Black")) {
                black = "Black";
            }

            if (white.contains(forbidden) && !white.equals("White")) {
                white = "White";
            }

            if (white.equals("White") && black.equals("Black")) break;
        }

        return black + white + "_" + phase.name().toUpperCase() + "_" + randomId + ".sgf";
    }

    public static String getPlayersName(byte[] file) {

        SgfCollection collection = new SgfParser().parse(file);
        SgfGameTree game = collection.game(0);
        SgfNode root = game.root();

        String black = root.value("PB").orElse("unknown");

        String white = root.value("PW").orElse("unknown");


        return black + " vs " + white;

    }

}
