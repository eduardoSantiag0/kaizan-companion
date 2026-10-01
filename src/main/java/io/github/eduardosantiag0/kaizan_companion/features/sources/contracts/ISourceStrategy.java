package io.github.eduardosantiag0.kaizan_companion.features.sources.contracts;

public interface ISourceStrategy {
    byte[] downloadGameById(String url);
    byte[] downloadLastGame(Long id);
}
