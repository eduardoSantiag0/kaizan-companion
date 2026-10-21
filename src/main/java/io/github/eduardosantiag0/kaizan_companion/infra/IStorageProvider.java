package io.github.eduardosantiag0.kaizan_companion.infra;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.GamePhase;
import org.springframework.core.io.Resource;

public interface IStorageProvider {
    String store(byte[] file);
    String store(byte[] file, GamePhase phase);
    Boolean exists(String key);
    Resource load(String key);
    void delete(String key);

}
