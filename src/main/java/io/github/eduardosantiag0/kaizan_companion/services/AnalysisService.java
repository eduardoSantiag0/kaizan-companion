package io.github.eduardosantiag0.kaizan_companion.services;

import io.github.eduardosantiag0.kaizan_companion.interfaces.ICommands;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService implements ICommands {

    private final DownloadGameByIdUseCase downloadGameByIdUseCase;

    public AnalysisService(DownloadGameByIdUseCase downloadGameByIdUseCase) {
        this.downloadGameByIdUseCase = downloadGameByIdUseCase;
    }

    @Override
    public void study(String url) {
        downloadGameByIdUseCase.execute(url);
    }

    @Override
    public void collection() {

    }

    @Override
    public void addEmail() {

    }

    @Override
    public void addGoodleDrive() {

    }

    @Override
    public void addMyProfile() {

    }

    @Override
    public void latest() {

    }
}
