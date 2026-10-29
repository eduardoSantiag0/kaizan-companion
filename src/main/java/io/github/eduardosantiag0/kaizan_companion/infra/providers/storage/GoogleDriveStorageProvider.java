package io.github.eduardosantiag0.kaizan_companion.infra.providers.storage;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.GamePhase;
import io.github.eduardosantiag0.kaizan_companion.infra.IStorageProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "storage.provider",
        havingValue = "google-drive"
)
public class GoogleDriveStorageProvider implements IStorageProvider {
    @Override
    public String store(byte[] file) {
        return "";
    }

    @Override
    public String store(byte[] file, GamePhase phase) {
        return "";
    }

    @Override
    public Boolean exists(String key) {
        return null;
    }

    @Override
    public Resource load(String key) {
        return null;
    }

    @Override
    public void delete(String key) {

    }
}
