package io.github.eduardosantiag0.kaizan_companion.services;

import io.github.eduardosantiag0.kaizan_companion.interfaces.ISource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class DownloadGameByIdUseCase {

    private final ISource source;

    public DownloadGameByIdUseCase(ISource source) {
        this.source = source;
    }

    public void execute(String url) {
        source.downloadGame(url);
    }
}
