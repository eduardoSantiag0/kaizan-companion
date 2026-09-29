package io.github.eduardosantiag0.kaizan_companion.features.files;

import io.github.eduardosantiag0.kaizan_companion.infra.IStorageProvider;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class FileUploadService {
    private final IStorageProvider storageProvider;


    public FileUploadService(IStorageProvider storageProvider) {
        this.storageProvider = storageProvider;
    }

    @Transactional()
    public void uploadFile(byte[] data) {
        storageProvider.store(data);
    }
}

