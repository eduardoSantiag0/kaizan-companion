package io.github.eduardosantiag0.kaizan_companion.features.analysis;

import io.github.eduardosantiag0.kaizan_companion.domain.exception.CustomExceptionHandler;
import io.github.eduardosantiag0.kaizan_companion.features.files.FileUploadService;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.ICommands;
import io.github.eduardosantiag0.kaizan_companion.features.sources.usecases.DownloadGameByIdUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@Service
public class AnalysisService implements ICommands {

    private final DownloadGameByIdUseCase downloadGameByIdUseCase;
    private final FileUploadService fileUploadService;

    private final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    public AnalysisService(DownloadGameByIdUseCase downloadGameByIdUseCase, FileUploadService fileUploadService) {
        this.downloadGameByIdUseCase = downloadGameByIdUseCase;
        this.fileUploadService = fileUploadService;
    }

    private static boolean isValidURL(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void study(String url) {
        if (!isValidURL(url)) {
            return;
        }
        byte[]fileData =  downloadGameByIdUseCase.execute(url);

        if (fileData == null) {
            logger.error("file data is null");
        }

        // Savar em algum lugar esses bytes
        fileUploadService.uploadFile(fileData);
        // Salvar no chat
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
