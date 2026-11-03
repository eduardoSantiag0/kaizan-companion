package io.github.eduardosantiag0.kaizan_companion.infra.providers.storage;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.GamePhase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStorageProviderTest {

    @TempDir
    Path tempRoot;

    private LocalStorageProvider newProvider(Path uploadDir) throws Exception {
        LocalStorageProvider provider = new LocalStorageProvider();
        Field field = LocalStorageProvider.class.getDeclaredField("fileUploadDir");
        field.setAccessible(true);
        field.set(provider, uploadDir.toString());
        provider.init();
        return provider;
    }

    @Test
    void store_mustNeverWriteOutsideTheConfiguredUploadDirectory() throws Exception {
        Path uploadDir = tempRoot.resolve("app-data").resolve("uploads");
        LocalStorageProvider provider = newProvider(uploadDir);

        String sgf = "(;GM[1]FF[4]PB[../../escaped]PW[x];B[aa])";
        UUID id = UUID.randomUUID();

        Path written = provider.store(sgf.getBytes(StandardCharsets.UTF_8), GamePhase.opening, id);

        System.out.println(written.getFileName());
        assertTrue(written.normalize().startsWith(uploadDir.normalize()),
                "a crafted player name escaped the configured upload directory: " + written);
    }
}
