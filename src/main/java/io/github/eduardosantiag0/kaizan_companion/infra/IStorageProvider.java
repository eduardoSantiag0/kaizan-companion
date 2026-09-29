package io.github.eduardosantiag0.kaizan_companion.infra;

import org.springframework.core.io.Resource;

public interface IStorageProvider {
    String store(byte[] file);
    Boolean exists(String key);
    Resource load(String key);
    void delete(String key);

}
