package io.github.eduardosantiag0.kaizan_companion.infra.providers.storage;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.storage.StorageException;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.GamePhase;
import io.github.eduardosantiag0.kaizan_companion.features.storage.messaging.DriveStorageEvent;
import io.github.eduardosantiag0.kaizan_companion.infra.IStorageProvider;
import io.github.eduardosantiag0.kaizan_companion.infra.security.GoogleTokenService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import com.google.api.services.drive.model.File;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.github.eduardosantiag0.kaizan_companion.utils.FilenamePattern.resolveFileName;
import static java.util.Currency.getInstance;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "storage.provider",
        havingValue = "google-drive"
)
public class GoogleDriveStorageProvider implements IStorageProvider {

    private final GoogleTokenService tokenService;
    private final TelegramChatRepository chatRepository;

    private Path tempPath;

    private final String TEMP_DIR = "tmp/drive";
    private final String APPLICATION_NAME = "Kaizen Companion";
    private final String DIR_NAME = "kaizen_games";
    private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

    record GameToUpload (String fileName, byte[] data, Path path){};

    @PostConstruct
    public void init() {
        log.info("Using Drive Storage");

        try {
            this.tempPath = Files.createDirectories(
                    Paths.get(TEMP_DIR)
                            .toAbsolutePath()
                            .normalize()
            );
        } catch (IOException e) {
            throw new StorageException(
                    "Could not initialize storage directory: " + e.getMessage()
            );
        }
    }


    private Drive getCredentials(Long chatId) {
        String accessToken = tokenService.getValidAccessToken(chatId);

        Credential credential = new Credential(
                BearerToken.authorizationHeaderAccessMethod()
        ).setAccessToken(accessToken);

        return new Drive.Builder
                (
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
                )
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

//    MimeType de pasta no Google Drive é 'application/vnd.google-apps.folder'

    private String createDirectory(Drive driveInstance) {
        File folderMetadata = new File();
        folderMetadata.setName(DIR_NAME);
        folderMetadata.setMimeType(FOLDER_MIME_TYPE);

        try {
            File folder = driveInstance.files().create(folderMetadata)
                    .setFields("id")
                    .execute();
            return folder.getId();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Drive directory", e);
        }
    }

    public void uploadFile(DriveStorageEvent event) {
        Drive driveInstance = getCredentials(event.chatId());

        byte[][] sgfsInBytes = event.sgfInBytes();

        List<GameToUpload> games = new ArrayList<>();

        UUID uuid = UUID.randomUUID();

        Path path0 = null, path1 = null, path2 = null;

        try {
            path0 = this.store(sgfsInBytes[0], GamePhase.opening, uuid);
            path1 = this.store(sgfsInBytes[1], GamePhase.middlegame, uuid);
            path2 = this.store(sgfsInBytes[2], GamePhase.endgame, uuid);

            games.add(new GameToUpload(path0.getFileName().toString(), sgfsInBytes[0], path0));
            games.add(new GameToUpload(path1.getFileName().toString(), sgfsInBytes[1], path1));
            games.add(new GameToUpload(path2.getFileName().toString(), sgfsInBytes[2], path2));

            TelegramChatEntity chatEntity = chatRepository.findById(event.chatId())
                    .orElseThrow(() -> new RuntimeException("Chat not found!"));

            String folderId = chatEntity.getDriveFolderId();
            if (folderId == null) {
                folderId = createDirectory(driveInstance);
                chatEntity.setDriveFolderId(folderId);
                chatRepository.save(chatEntity);
            }

            for (GameToUpload game : games) {
                File fileMetadata = new File();
                fileMetadata.setParents(Collections.singletonList(folderId));
                fileMetadata.setName(game.fileName());

                driveInstance.files().create(fileMetadata, new InputStreamContent(
                        "application/x-go-sgf",
                        new ByteArrayInputStream(game.data()))
                ).setFields("id").execute();
            }

        } catch (Exception e) {
            log.error("Error during Google Drive upload process for chat {}", event.chatId(), e);
            throw new RuntimeException("Upload failed", e);
        } finally {
            if (path0 != null) delete(path0);
            if (path1 != null) delete(path1);
            if (path2 != null) delete(path2);
        }
    }



    @Override
    public void delete(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String key) {

    }

    @Override
    public Path store(byte[] file, GamePhase phase, UUID uuid) {
        String fileName = resolveFileName(file, phase, uuid);
        Path filePath = tempPath.resolve(fileName);

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
        return null;
    }

    @Override
    public Resource load(String key) {
        return null;
    }



}
