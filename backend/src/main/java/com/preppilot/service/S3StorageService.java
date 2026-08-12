package com.preppilot.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
public class S3StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    @Value("${app.aws.s3.bucket}")
    private String bucket;

    @Value("${app.aws.s3.region}")
    private String region;

    @Value("${app.aws.s3.access-key}")
    private String accessKey;

    @Value("${app.aws.s3.secret-key}")
    private String secretKey;

    private S3Client s3Client;

    @PostConstruct
    void init() {
        try {
            if (accessKey != null && !accessKey.isBlank() && !accessKey.startsWith("your_")) {
                this.s3Client = S3Client.builder()
                        .region(Region.of(region))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                        .build();
            }
        } catch (Exception e) {
            log.warn("Failed to initialize AWS S3 client: {}", e.getMessage());
        }
    }

    /** Uploads a resume PDF and returns the S3 object key. */
    public String uploadResume(UUID userId, String originalFileName, byte[] fileBytes) {
        String key = "resumes/%s/%s-%s".formatted(userId, UUID.randomUUID(), originalFileName);
        if (s3Client != null) {
            try {
                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .contentType("application/pdf")
                                .build(),
                        RequestBody.fromBytes(fileBytes)
                );
            } catch (Exception e) {
                log.warn("S3 upload failed for key: {}. Error: {}. Proceeding with local key.", key, e.getMessage());
            }
        } else {
            log.info("AWS S3 not configured. Proceeding with local key: {}", key);
        }
        return key;
    }
}

