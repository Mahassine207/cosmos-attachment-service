package ma.sofisoft.mappers;

import ma.sofisoft.dtos.AttachmentResponse;
import ma.sofisoft.dtos.CreateAttachmentRequest;
import ma.sofisoft.entities.Attachment;
import ma.sofisoft.entities.Photo;
import org.mapstruct.*;

@Mapper(componentModel = "cdi")
public interface AttachmentMapper {

    // Attachment → Response
    @Mapping(target = "url", ignore = true)
    AttachmentResponse toResponse(Attachment entity);

    // Photo → Response
    @Mapping(target = "url", ignore = true)
    @Mapping(target = "mimeType", ignore = true)
    @Mapping(target = "originalFilename", ignore = true)
    @Mapping(target = "sizeBytes", ignore = true)
    AttachmentResponse toResponse(Photo entity);

    // Request → Attachment
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerType", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "url", ignore = true)
    @Mapping(target = "mimeType", ignore = true)
    @Mapping(target = "originalFilename", ignore = true)
    @Mapping(target = "sizeBytes", ignore = true)
    @Mapping(target = "minioKey", ignore = true)
    @Mapping(target = "bucket", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Attachment toEntity(CreateAttachmentRequest request);
}
