package ma.sofisoft.services;

import ma.sofisoft.dtos.AttachmentResponse;
import ma.sofisoft.dtos.CreateAttachmentRequest;
import ma.sofisoft.enums.OwnerType;
import java.util.List;
import java.util.UUID;

public interface AttachmentService {
    AttachmentResponse upload(OwnerType ownerType, UUID ownerId, CreateAttachmentRequest request);
    AttachmentResponse getById(UUID id);
    List<AttachmentResponse> getByOwner(OwnerType ownerType, UUID ownerId);
    void delete(UUID id);
    void deleteByOwner(OwnerType ownerType, UUID ownerId);
}