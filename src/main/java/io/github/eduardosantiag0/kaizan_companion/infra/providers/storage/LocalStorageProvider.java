package io.github.eduardosantiag0.kaizan_companion.infra.providers.storage;

import io.github.eduardosantiag0.kaizan_companion.domain.exception.storage.StorageException;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.GamePhase;
import io.github.eduardosantiag0.kaizan_companion.infra.IStorageProvider;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.eduardosantiag0.kaizan_companion.utils.FilenamePattern.resolveFileName;

@Service
@Profile("dev")
@ConditionalOnProperty(
        name = "storage.provider",
        havingValue = "local"
)
public class LocalStorageProvider implements IStorageProvider {

    @Value("${file.upload-dir}")
    private String fileUploadDir;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Path PATH_FOLDER;

    @PostConstruct
    public void init() {
        logger.info("Using storage provider: local");

        try {
            this.PATH_FOLDER = Files.createDirectories(
                    Paths.get(fileUploadDir)
                            .toAbsolutePath()
                            .normalize()
            );
        } catch (IOException e) {
            throw new StorageException(
                    "Could not initialize storage directory: " + e.getMessage()
            );
        }
    }

    @Override
    public String store(byte[] file) {

        // Extract sgf
        //TODO tirar esse phase
        String fileName = resolveFileName(file, GamePhase.opening);
        Path filePath = PATH_FOLDER.resolve(fileName);

        try {
            Files.write(filePath, file);
            logger.debug("\n\nFile name: {} in Path: {}\n\n", fileName, filePath );
            return fileName;

        } catch (IOException e) {
            throw new StorageException(
                    "Could not store file: " + fileName
            );
        }
    }

    @Override
    public String store(byte[] file, GamePhase phase) {
        // Extract sgf
        String fileName = resolveFileName(file, phase);
        Path filePath = PATH_FOLDER.resolve(fileName);

        try {
            Files.write(filePath, file);
            logger.debug("\n\nFile name: {} in Path: {}\n\n", fileName, filePath );
            return fileName;

        } catch (IOException e) {
            throw new StorageException(
                    "Could not store file: " + fileName
            );
        }
    }

    @Override
    public Boolean exists(String key) {
        Path physicalFilePath = Path.of(key).normalize();
        return Files.exists(physicalFilePath);
    }

    @Override
    public Resource load(String key) {
        try {
            Path physicalFilePath = Path.of(key).normalize();

            Resource resource = new UrlResource(physicalFilePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new StorageException("Could not read file: " + physicalFilePath);
            }

            return resource;
        } catch (MalformedURLException e) {
            throw new StorageException("Failed to read file (Malformed URL): " + key);
        }
    }

    @Override
    public void delete(String key) {
        Path physicalFilePath = Path.of(key).normalize();
        try {
            Files.delete(physicalFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
