package ma.sofisoft.entities;

import ma.sofisoft.enums.OwnerType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attachment", indexes = {
        @Index(name = "idx_attachment_owner",
                columnList = "owner_type, owner_id"),
        @Index(name = "idx_attachment_type",
                columnList = "mime_type")
})
@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor @Builder
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid",
            updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private OwnerType ownerType;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "minio_key", nullable = false, columnDefinition = "TEXT")
    private String minioKey;

    @Column(name = "bucket", nullable = false, columnDefinition = "TEXT")
    private String bucket;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
}
