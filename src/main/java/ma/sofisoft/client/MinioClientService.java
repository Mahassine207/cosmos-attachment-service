package ma.sofisoft.client;

import io.minio.*;
import io.minio.http.Method;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Slf4j
@ApplicationScoped
public class MinioClientService {

    @Inject
    MinioClient minioClient;

    // UPLOAD
    public void upload(String bucket,
                       String minioKey,
                       Path filePath,
                       String mimeType) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucket)
                            .build());

            // Create the bucket if not exists
            if (!exists) {
                log.info("Creation of the bucket : {}", bucket);
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucket)
                                .build());
            }

            // Upload file
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket)
                            .object(minioKey)
                            .filename(filePath.toString())
                            .contentType(mimeType)
                            .build());

            log.info("Fichier uploaded : {}/{}", bucket, minioKey);

        } catch (Exception e) {
            log.error("Erreur upload MinIO : {}", e.getMessage());
            throw new RuntimeException(
                    "MinIO upload failed : " + e.getMessage());
        }
    }

    // PRESIGNED URL
    public String getPresignedUrl(String bucket,
                                  String minioKey) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(minioKey)
                            .expiry(15, TimeUnit.MINUTES)
                            .method(Method.GET)
                            .build());

            log.debug("Presigned URL generated for : {}/{}",
                    bucket, minioKey);
            return url;

        } catch (Exception e) {
            log.error("Erreur Presigned URL : {}", e.getMessage());
            throw new RuntimeException(
                    "MinIO presigned URL failed : " + e.getMessage());
        }
    }

    // DELETE
    public void delete(String bucket, String minioKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(minioKey)
                            .build());

            log.info("File deleted : {}/{}", bucket, minioKey);

        } catch (Exception e) {
            log.error("Erreur suppression MinIO : {}",
                    e.getMessage());
            throw new RuntimeException(
                    "MinIO delete failed : " + e.getMessage());
        }
    }
}

