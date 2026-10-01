package io.github.eduardosantiag0.kaizan_companion.features.sources.usecases;

import io.github.eduardosantiag0.kaizan_companion.features.sources.contracts.ISourceStrategy;
import io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.OgsGameDownloader;
import org.springframework.stereotype.Service;

@Service
public class SourceContextStrategy {

    private final ISourceStrategy source;

    public SourceContextStrategy(ISourceStrategy source) {
        this.source = source;
    }

    public byte[] execute(String url) {
        return source.downloadGameById(url);
    }

    public ISourceStrategy setStrategy (String url) {
        if (url.contains("https://online-go.com")) {
            return new OgsGameDownloader();
        } else {
            return null;
        }
    }
}
