package io.github.eduardosantiag0.kaizan_companion.infra;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.GamePhase;
import org.springframework.core.io.Resource;

import java.nio.file.Path;
import java.util.UUID;

public interface IStorageProvider {
    Path store(byte[] file, GamePhase phase, UUID uuid);
    Boolean exists(String key);
    Resource load(String key);
    void delete(String key);
    void delete(Path path);

}
