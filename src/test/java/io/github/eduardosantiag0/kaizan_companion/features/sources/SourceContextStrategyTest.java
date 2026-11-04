package io.github.eduardosantiag0.kaizan_companion.features.sources;

import io.github.eduardosantiag0.kaizan_companion.features.sources.contracts.ISourceStrategy;
import io.github.eduardosantiag0.kaizan_companion.features.sources.dgs.DragonServerGameDownloader;
import io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.OgsGameDownloader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class SourceContextStrategyTest {

    private final OgsGameDownloader ogs = mock(OgsGameDownloader.class);
    private final DragonServerGameDownloader dgs = mock(DragonServerGameDownloader.class);
    private final SourceContextStrategy strategy = new SourceContextStrategy(ogs, dgs);

    @Test
    void setStrategy_treatsALegitimateOgsUrlAsOgs() {
        assertSame(ogs, strategy.setStrategy("https://online-go.com/game/123456"));
    }

    @Test
    void setStrategy_mustRejectAHostThatOnlyLooksLikeOnlineGoComAsASubdomainPrefix() {
        assertNull(strategy.setStrategy("https://online-go.com.attacker.example/game/1"));
    }

    @Test
    void setStrategy_mustRejectOnlineGoComAppearingOnlyInAQueryString() {
        assertNull(strategy.setStrategy("https://attacker.example/x?redirect=online-go.com"));
    }

    @Test
    void setStrategy_mustRejectDragonGoServerNetAppearingOnlyInAPathSegment() {
        assertNull(strategy.setStrategy("https://attacker.example/dragongoserver.net/x"));
    }

    @Test
    void setStrategy_returnsNull_forAnUnrelatedUrl() {
        assertNull(strategy.setStrategy("https://example.com/whatever"));
    }
}
