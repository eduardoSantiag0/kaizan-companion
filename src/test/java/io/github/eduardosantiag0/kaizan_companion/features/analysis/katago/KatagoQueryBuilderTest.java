package io.github.eduardosantiag0.kaizan_companion.features.analysis.katago;

import io.github.eduardosantiag0.sgf.model.SgfCollection;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import io.github.eduardosantiag0.sgf.parser.SgfParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class KatagoQueryBuilderTest {

    private final KatagoQueryBuilder builder = new KatagoQueryBuilder();

    private static SgfGameTree parse(String sgf) {
        SgfCollection collection = new SgfParser().parse(sgf.getBytes(StandardCharsets.UTF_8));
        return collection.game(0);
    }

    @Test
    void buildQuery_mustNotThrow_whenPlayerRankTagsAreAbsent() {
        SgfGameTree game = parse("(;GM[1]FF[4]SZ[19]KM[6.5]RU[Chinese];B[pd];W[dp])");

        assertDoesNotThrow(() -> builder.buildQuery(game, UUID.randomUUID()));
    }

    @Test
    void buildQuery_mustNotThrow_forARectangularBoard() {
        SgfGameTree game = parse(
                "(;GM[1]FF[4]SZ[19:13]KM[6.5]RU[Chinese]WR[5d]BR[5d];B[pd];W[dp])");

        assertDoesNotThrow(() -> builder.buildQuery(game, UUID.randomUUID()));
    }
}
