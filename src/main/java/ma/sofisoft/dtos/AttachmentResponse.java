package ma.sofisoft.dtos;

import lombok.Data;
import ma.sofisoft.enums.OwnerType;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AttachmentResponse {

    private UUID id;

    private OwnerType ownerType;

    private UUID ownerId;

    private String url;

    private String mimeType;

    private String originalFilename;

    private Long sizeBytes;

    private String createdBy;

    private LocalDateTime createdAt;
}
