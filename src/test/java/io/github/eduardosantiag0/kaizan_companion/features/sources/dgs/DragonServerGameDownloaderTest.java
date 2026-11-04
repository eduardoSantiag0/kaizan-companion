package io.github.eduardosantiag0.kaizan_companion.features.sources.dgs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class DragonServerGameDownloaderTest {

    private final DragonServerGameDownloader downloader = new DragonServerGameDownloader();

    @Test
    void extractId_fromAUrlWhereGidIsTheLastParameter() {
        assertEquals("1234567", downloader.extractId("https://www.dragongoserver.net/sgf.php?gid=1234567"));
    }

    @Test
    void extractId_mustStripTrailingParametersAfterGid() {
        assertEquals("1234567", downloader.extractId(
                "https://www.dragongoserver.net/sgf.php?gid=1234567&owned_comments=Y"));
    }

    @Test
    void extractId_throwsIllegalArgumentException_whenUrlHasNoGidParameter() {
        assertThrows(IllegalArgumentException.class,
                () -> downloader.extractId("https://www.dragongoserver.net/sgf.php"));
    }
}
