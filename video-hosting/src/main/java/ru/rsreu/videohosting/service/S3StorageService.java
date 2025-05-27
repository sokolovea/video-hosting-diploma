package ru.rsreu.videohosting.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Service
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final String bucketName;
    private final String providerSite;

    public S3StorageService(
            @Value("${custom.s3.access-key}") String accessKey,
            @Value("${custom.s3.secret-key}") String secretKey,
            @Value("${custom.s3.region}") String region,
            @Value("${custom.s3.bucket-name}") String bucketName,
            @Value("${custom.s3.provider-site}") String providerSite) {
        this.bucketName = bucketName;

        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.providerSite = providerSite;
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(providerSite))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .httpClientBuilder(ApacheHttpClient.builder())
                .build();
    }

    @Override
    public String store(MultipartFile file, ContentMultimediaType contentMultimediaType) {
        try {
            String key = contentMultimediaType.name().toLowerCase() + "/" + UUID.randomUUID() + ".mp4";;

            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

            return String.format(providerSite + "/%s/%s", bucketName, key);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки файла в S3: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String key) {
        key = key.replace(providerSite, "");
        key = key.replace("/" + bucketName + "/", "");
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }

    @Override
    public String resolveUrl(String filePath) {
        return String.format(providerSite + "/%s/%s", bucketName, filePath);
    }
}
