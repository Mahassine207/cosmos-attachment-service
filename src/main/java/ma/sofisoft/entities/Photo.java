package ma.sofisoft.entities;

import jakarta.persistence.*;
import lombok.*;
import ma.sofisoft.enums.OwnerType;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "photos", indexes = {
        @Index(name = "idx_photo_owner",
                columnList = "owner_type, owner_id"),
        @Index(name = "idx_photo_main",
                columnList = "is_main")
})
@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor @Builder
public class Photo {

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

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "is_main", nullable = false)
    private Boolean isMain;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
}