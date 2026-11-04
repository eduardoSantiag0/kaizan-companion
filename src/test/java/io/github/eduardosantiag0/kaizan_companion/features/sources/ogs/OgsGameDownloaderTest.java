package io.github.eduardosantiag0.kaizan_companion.features.sources.ogs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OgsGameDownloaderTest {

    private final OgsGameDownloader downloader = new OgsGameDownloader();

    @Test
    void extractId_fromABareGameUrl() {
        assertEquals("123456", downloader.extractId("https://online-go.com/game/123456"));
    }

    @Test
    void extractId_mustStripATrailingQueryString() {
        assertEquals("123456", downloader.extractId("https://online-go.com/game/123456?source=email"));
    }

    @Test
    void extractId_mustStripATrailingPathSegment() {
        assertEquals("123456", downloader.extractId("https://online-go.com/game/123456/some-review-slug"));
    }

    @Test
    void extractId_throwsIllegalArgumentException_whenUrlHasNoGameSegment() {
        assertThrows(IllegalArgumentException.class,
                () -> downloader.extractId("https://online-go.com/"));
    }
}
