package io.github.eduardosantiag0.kaizan_companion.features.sources;

import io.github.eduardosantiag0.kaizan_companion.features.sources.contracts.ISourceStrategy;
import io.github.eduardosantiag0.kaizan_companion.features.sources.dgs.DragonServerGameDownloader;
import io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.OgsGameDownloader;
import org.springframework.stereotype.Service;

@Service
public class SourceContextStrategy {

    private final OgsGameDownloader ogsGameDownloader;
    private final DragonServerGameDownloader dragonServerGameDownloader;


    public SourceContextStrategy(OgsGameDownloader ogsGameDownloader, DragonServerGameDownloader dragonServerGameDownloader) {
        this.ogsGameDownloader = ogsGameDownloader;
        this.dragonServerGameDownloader = dragonServerGameDownloader;
    }

    public ISourceStrategy setStrategy (String url) {
        System.out.println(url);
        if (url.contains("online-go.com")) {
            return ogsGameDownloader;
        } else if (url.contains("dragongoserver.net")) {
            return dragonServerGameDownloader;
        }
        else {
            return null;
        }
    }
}
