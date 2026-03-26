package ma.sofisoft.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

@Singleton
public class JacksonConfig implements ObjectMapperCustomizer {

    @Override
    public void customize(ObjectMapper mapper) {
        // Support natif des LocalDateTime (createdAt, etc.)
        mapper.registerModule(new JavaTimeModule());

        // Empêche le format [2026,3,25] et force le format ISO "2026-03-25T..."
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}