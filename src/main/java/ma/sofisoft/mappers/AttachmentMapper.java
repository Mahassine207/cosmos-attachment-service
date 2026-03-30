package ma.sofisoft.mappers;

import ma.sofisoft.dtos.AttachmentResponse;
import ma.sofisoft.dtos.CreateAttachmentRequest;
import ma.sofisoft.entities.Attachment;
import ma.sofisoft.entities.Photo;
import org.mapstruct.*;

@Mapper(componentModel = "cdi")
public interface AttachmentMapper {

    // Document (Attachment) → Response
    @Mapping(target = "url", ignore = true)
    AttachmentResponse toResponse(Attachment entity);

    // Photo → Response
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "ownerType", source = "entity.ownerType")
    @Mapping(target = "ownerId", source = "entity.ownerId")
    @Mapping(target = "createdBy", source = "entity.createdBy")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "url", source = "url")
    @Mapping(target = "originalFilename", source = "filename")
    @Mapping(target = "mimeType", source = "mime")
    @Mapping(target = "sizeBytes", constant = "0L")
    AttachmentResponse photoToResponse(Photo entity, String filename, String mime, String url);

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
    @Mapping(target = "createdBy", source = "request.createdBy")
    Attachment toEntity(CreateAttachmentRequest request);
}