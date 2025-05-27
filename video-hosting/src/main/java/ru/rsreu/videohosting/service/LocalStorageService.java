package ru.rsreu.videohosting.service;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.MultipartConfigElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private final Path rootLogoLocation = Paths.get("./uploads/photo");
    private final Path rootImagesLocation = Paths.get("./uploads/images");
    private final Path rootVideosLocation = Paths.get("./uploads/videos");

    public LocalStorageService() {
        try {
            Files.createDirectories(rootLogoLocation);
            Files.createDirectories(rootImagesLocation);
            Files.createDirectories(rootVideosLocation);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папки для загрузки", e);
        }
    }

    public String store(MultipartFile file, ContentMultimediaType contentMultimediaType) {
        try {
            Path targetDirectory;
            switch (contentMultimediaType) {
                case LOGO:
                    targetDirectory = rootLogoLocation;
                    break;
                case VIDEO_IMAGE:
                    targetDirectory = rootImagesLocation;
                    break;
                case VIDEO:
                    targetDirectory = rootVideosLocation;
                    break;
                default:
                    throw new IllegalArgumentException("Неизвестный тип контента");
            }

            Files.createDirectories(targetDirectory);

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                throw new IllegalArgumentException("Файл не имеет имени");
            }
            String filename = UUID.randomUUID() + "_" + Base64.getEncoder().encodeToString(originalFilename.getBytes()) + ".mp4";

            Path targetPath  = Paths.get(targetDirectory + "/" + filename);
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath);

            return targetPath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения файла: " + e.getMessage(), e);
        }
    }

    private Path resolvePath(String filePath) {
        return Paths.get(filePath).toAbsolutePath().normalize();
    }

    public void deleteFile(String filePath) {
        try {
            Path path = resolvePath(filePath); //.substring(1)
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка удаления файла: " + e.getMessage(), e);
        }
    }

    @Override
    public String resolveUrl(String filePath) {
        return Paths.get(filePath).toAbsolutePath().normalize().toString();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation(System.getProperty("java.io.tmpdir"));
        return factory.createMultipartConfig();
    }
}

