package ma.sofisoft.config;

import io.minio.MinioClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class MinioConfig {

    // On ajoute "cosmos." devant pour correspondre au properties
    @ConfigProperty(name = "cosmos.minio.url")
    String minioUrl;

    @ConfigProperty(name = "cosmos.minio.access-key")
    String accessKey;

    @ConfigProperty(name = "cosmos.minio.secret-key")
    String secretKey;

    @Produces
    @ApplicationScoped
    public MinioClient minioClient() {
        log.info("Initialisation MinioClient sur l'URL : {}", minioUrl);
        return MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
    }
}