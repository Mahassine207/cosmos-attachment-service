package ma.sofisoft.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.nio.file.Path;

@Slf4j
@ApplicationScoped
public class MinioClientService {

    @Inject
    S3Client s3Client;

    private void setBucketPublicPolicy(String bucket) {
        String publicPolicy = """
            {
              "Version":"2012-10-17",
              "Statement":[
                {
                  "Effect":"Allow",
                  "Principal":"*",
                  "Action":["s3:GetObject"],
                  "Resource":["arn:aws:s3:::%s/*"]
                }
              ]
            }
            """.formatted(bucket);

        s3Client.putBucketPolicy(PutBucketPolicyRequest.builder()
                .bucket(bucket)
                .policy(publicPolicy)
                .build());
    }

    // UPLOAD
    public void upload(String bucket,
                       String minioKey,
                       Path filePath,
                       String mimeType) {
        try {
            // Create bucket if not exists
            boolean exists = bucketExists(bucket);
            if (!exists) {
                log.info("Creating bucket: {}", bucket);
                s3Client.createBucket(
                        CreateBucketRequest.builder()
                                .bucket(bucket)
                                .build());
            }
            setBucketPublicPolicy(bucket);

            // Upload file
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

    // Genarate Url
    public String getPublicUrl(String bucket, String minioKey) {
        return String.format("http://%s/%s/%s", "localhost:9000", bucket, minioKey);
    }

    // DELETE
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

    // BUCKET EXISTS
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