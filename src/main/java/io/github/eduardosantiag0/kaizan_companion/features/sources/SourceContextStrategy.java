package io.github.eduardosantiag0.kaizan_companion.features.sources;

import io.github.eduardosantiag0.kaizan_companion.features.sources.contracts.ISourceStrategy;
import io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.OgsGameDownloader;
import org.springframework.stereotype.Service;

@Service
public class SourceContextStrategy {

    private final OgsGameDownloader ogsGameDownloader;

    public SourceContextStrategy(OgsGameDownloader ogsGameDownloader) {
        this.ogsGameDownloader = ogsGameDownloader;
    }

    public ISourceStrategy setStrategy (String url) {
        if (url.contains("online-go.com")) {
            return ogsGameDownloader;
        } else {
            return null;
        }
    }
}
