package io.github.eduardosantiag0.kaizan_companion.features.sources.usecases;

import io.github.eduardosantiag0.kaizan_companion.features.sources.contracts.ISource;
import org.springframework.stereotype.Service;

@Service
public class DownloadGameByIdUseCase {

    private final ISource source;

    public DownloadGameByIdUseCase(ISource source) {
        this.source = source;
    }

    public byte[] execute(String url) {
        return source.downloadGame(url);
    }
}
