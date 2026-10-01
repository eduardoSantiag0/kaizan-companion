package io.github.eduardosantiag0.kaizan_companion.features.sources.usecases;

import io.github.eduardosantiag0.kaizan_companion.features.sources.contracts.ISourceStrategy;
import org.springframework.stereotype.Service;

@Service
public class DownloadLastGameUseCase {
    private final ISourceStrategy source;


    public DownloadLastGameUseCase(ISourceStrategy source) {
        this.source = source;
    }

    public byte[] execute(Long id) {
        return source.downloadLastGame(id);
    }
}
