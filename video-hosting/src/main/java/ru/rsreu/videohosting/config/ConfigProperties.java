package ru.rsreu.videohosting.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ConfigProperties {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @Value("${custom.max-avatar-photo-size-mb}")
    private Long maxAvatarPhotoSizeMb;

    @Value("${custom.max-video-thumbnail-size-mb}")
    private Long maxVideoThumbnailSizeMb;

    @Value("${custom.s3-enable}")
    private boolean useS3;
}
