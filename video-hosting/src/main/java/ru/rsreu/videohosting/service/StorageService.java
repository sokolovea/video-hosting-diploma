package ru.rsreu.videohosting.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String store(MultipartFile file, ContentMultimediaType contentMultimediaType);
    void deleteFile(String filePath);
    String resolveUrl(String filePath);

    default boolean isStoredOnS3(String filePath) {
        return filePath.startsWith("http");
    }
}
