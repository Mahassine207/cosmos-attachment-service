package ma.sofisoft.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import java.nio.file.Path;
import java.time.Duration;

@Slf4j
@ApplicationScoped
public class MinioClientService {

    @Inject
    S3Client s3Client;

    @Inject
    S3Presigner s3Presigner;

    // ── UPLOAD ──
    public void upload(String bucket,
                       String minioKey,
                       Path filePath,
                       String mimeType) {
        try {
            // Créer bucket si non existant
            boolean exists = bucketExists(bucket);
            if (!exists) {
                log.info("Creating bucket: {}", bucket);
                s3Client.createBucket(
                        CreateBucketRequest.builder()
                                .bucket(bucket)
                                .build());
            }

            // Uploader le fichier
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(minioKey)
                            .contentType(mimeType)
                            .build(),
                    RequestBody.fromFile(filePath)
            );

            log.info("File uploaded: {}/{}", bucket, minioKey);

        } catch (Exception e) {
            log.error("MinIO upload error: {}", e.getMessage());
            throw new RuntimeException(
                    "MinIO upload failed: " + e.getMessage());
        }
    }

    // ── PRESIGNED URL ──
    public String getPresignedUrl(String bucket, String minioKey) {
        try {
            GetObjectPresignRequest presignRequest =
                    GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(15))
                            .getObjectRequest(
                                    GetObjectRequest.builder()
                                            .bucket(bucket)
                                            .key(minioKey)
                                            .build())
                            .build();

            String url = s3Presigner
                    .presignGetObject(presignRequest)
                    .url()
                    .toString();

            log.debug("Presigned URL generated: {}/{}", bucket, minioKey);
            return url;

        } catch (Exception e) {
            log.error("Presigned URL error: {}", e.getMessage());
            throw new RuntimeException(
                    "MinIO presigned URL failed: " + e.getMessage());
        }
    }

    // ── DELETE ──
    public void delete(String bucket, String minioKey) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(minioKey)
                            .build());

            log.info("File deleted: {}/{}", bucket, minioKey);

        } catch (Exception e) {
            log.error("MinIO delete error: {}", e.getMessage());
            throw new RuntimeException(
                    "MinIO delete failed: " + e.getMessage());
        }
    }

    // ── BUCKET EXISTS ──
    private boolean bucketExists(String bucket) {
        try {
            s3Client.headBucket(
                    HeadBucketRequest.builder()
                            .bucket(bucket)
                            .build());
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        }
    }
}