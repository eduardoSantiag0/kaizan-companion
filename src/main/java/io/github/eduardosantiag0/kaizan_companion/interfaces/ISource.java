package io.github.eduardosantiag0.kaizan_companion.interfaces;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

public interface ISource {
    void downloadGame(String url);
}
