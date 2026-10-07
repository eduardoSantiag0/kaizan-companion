package io.github.eduardosantiag0.kaizan_companion.features.sources.contracts;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public interface ISourceStrategy {
    ByteArrayResource downloadGameById(String url);
    ByteArrayResource downloadLastGame(Long id);

    String extractId(String url);
}
