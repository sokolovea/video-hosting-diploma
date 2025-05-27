package ru.rsreu.videohosting.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class HybridStorageService implements StorageService {

    private final LocalStorageService localStorageService;
    private final S3StorageService s3StorageService;
    private final boolean s3Enable;

    public HybridStorageService(LocalStorageService localStorageService,
                                S3StorageService s3StorageService,
                                @Value("${custom.s3-enable}") boolean s3Enable) {
        this.localStorageService = localStorageService;
        this.s3StorageService = s3StorageService;
        this.s3Enable = s3Enable;
    }

    @Override
    public String store(MultipartFile file, ContentMultimediaType contentMultimediaType) {
        if (s3Enable && (contentMultimediaType.equals(ContentMultimediaType.VIDEO) ||
                contentMultimediaType.equals(ContentMultimediaType.VIDEO_IMAGE))) {
            return s3StorageService.store(file, contentMultimediaType);
        } else {
            return localStorageService.store(file, contentMultimediaType);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        if (s3StorageService.isStoredOnS3(filePath)) {
            s3StorageService.deleteFile(filePath);
        } else {
            localStorageService.deleteFile(filePath);
        }
    }

    @Override
    public String resolveUrl(String filePath) {
        if (s3StorageService.isStoredOnS3(filePath)) {
            return s3StorageService.resolveUrl(filePath);
        } else {
            return localStorageService.resolveUrl(filePath);
        }
    }

    @Override
    public boolean isStoredOnS3(String filePath) {
        return s3StorageService.isStoredOnS3(filePath);
    }
}

