package io.github.eduardosantiag0.kaizan_companion.utils;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.GamePhase;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilenamePatternTest {

    private static byte[] sgfWithPlayers(String black, String white) {
        String sgf = "(;GM[1]FF[4]PB[" + black + "]PW[" + white + "];B[aa])";
        return sgf.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void resolveFileName_mustNotContainPathTraversalSequencesFromPlayerNames() {
        byte[] sgf = sgfWithPlayers("../../../../tmp/evil", "attacker");
        UUID id = UUID.randomUUID();

        String fileName = FilenamePattern.resolveFileName(sgf, GamePhase.opening, id);

        assertFalse(fileName.contains(".."), "player-supplied text must not survive into a path-like segment: " + fileName);
        assertFalse(fileName.contains("/"), "player-supplied text must not introduce a path separator: " + fileName);
        assertFalse(fileName.contains("\\"), "player-supplied text must not introduce a path separator: " + fileName);
    }

}
