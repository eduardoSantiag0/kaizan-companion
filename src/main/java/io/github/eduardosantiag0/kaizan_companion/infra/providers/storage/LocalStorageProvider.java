package io.github.eduardosantiag0.kaizan_companion.infra.providers.storage;

import io.github.eduardosantiag0.kaizan_companion.domain.exception.storage.StorageException;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.GamePhase;
import io.github.eduardosantiag0.kaizan_companion.infra.IStorageProvider;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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
import java.util.UUID;

import static io.github.eduardosantiag0.kaizan_companion.utils.FilenamePattern.resolveFileName;

@Service
@Profile("dev")
@Slf4j
@ConditionalOnProperty(
        name = "storage.provider",
        havingValue = "local"
)
public class LocalStorageProvider implements IStorageProvider {

    @Value("${file.upload-dir}")
    private String fileUploadDir;
    private Path PATH_FOLDER;

    @PostConstruct
    public void init() {
        log.info("Using storage provider: local");

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
    public Path store(byte[] file, GamePhase phase, UUID uuid) {
        // Extract sgf
        String fileName = resolveFileName(file, phase, uuid);
        Path filePath = PATH_FOLDER.resolve(fileName);

        try {
            var path = Files.write(filePath, file);
            log.debug("\n\nFile name: {} in Path: {}\n\n", fileName, filePath );
            return path;

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

    @Override
    public void delete(Path path) {

    }
}
