package ma.sofisoft.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.client.MinioClientService;
import ma.sofisoft.config.TenantContext;
import ma.sofisoft.dtos.AttachmentResponse;
import ma.sofisoft.dtos.CreateAttachmentRequest;
import ma.sofisoft.entities.Attachment;
import ma.sofisoft.enums.OwnerType;
import ma.sofisoft.exceptions.AttachmentNotFoundException;
import ma.sofisoft.exceptions.BusinessException;
import ma.sofisoft.mappers.AttachmentMapper;
import ma.sofisoft.repositories.AttachmentRepository;
import java.util.List;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class AttachmentServiceImpl implements AttachmentService {

    @Inject
    AttachmentRepository attachmentRepository;

    @Inject
    MinioClientService minioClientService;

    @Inject
    AttachmentMapper attachmentMapper;

    @Inject
    TenantContext tenantContext;

    private static final long MAX_SIZE = 20L * 1024 * 1024; // 20MB

    // UPLOAD
    @Override
    @Transactional
    public AttachmentResponse upload(OwnerType ownerType,
                                     UUID ownerId,
                                     CreateAttachmentRequest request) {

        // 1. Validation présence fichier
        if (request.getFile() == null ||
                request.getFile().uploadedFile() == null) {
            throw new BusinessException(
                    "Missing file",
                    "MISSING_FILE",
                    400);
        }

        // 2. Validation taille (max 20MB)
        if (request.getFile().size() > MAX_SIZE) {
            throw new BusinessException(
                    "File too large (max 20MB)",
                    "FILE_TOO_LARGE",
                    413);
        }

        // 3. Validation MIME
        String mimeType = request.getFile().contentType();
        if (!isAllowedMimeType(mimeType)) {
            log.warn("Unsupported MIME type: {}", mimeType);
            throw new BusinessException(
                    "File type not supported: " + mimeType,
                    "INVALID_MIME_TYPE",
                    415);
        }

        // 4. Extraction infos fichier
        String filename = request.getFile().fileName();
        long sizeBytes  = request.getFile().size();

        // Extraction extension
        String extension = "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = filename.substring(dotIndex + 1).toLowerCase();
        }

        // 5. Générer UUID + clé MinIO
        UUID attachmentId = UUID.randomUUID();
        String bucket     = tenantContext.getBucket();
        String minioKey   = tenantContext.buildMinioKey(
                ownerType.name(),
                ownerId.toString(),
                attachmentId.toString(),
                extension
        );

        log.info("Uploading file: bucket={}, key={}", bucket, minioKey);

        // 6. Stockage MinIO
        minioClientService.upload(
                bucket,
                minioKey,
                request.getFile().uploadedFile(),
                mimeType
        );

        // 7. Générer URL
        String url = minioClientService.getPresignedUrl(bucket, minioKey);

        // 8. Persistance PostgreSQL
        Attachment entity = Attachment.builder()
                .id(attachmentId)
                .ownerType(ownerType)
                .ownerId(ownerId)
                .originalFilename(filename)
                .mimeType(mimeType)
                .sizeBytes(sizeBytes)
                .minioKey(minioKey)
                .bucket(bucket)
                .url(url)
                .createdBy(request.getCreatedBy())
                .build();

        attachmentRepository.persist(entity);
        log.info("Attachment persisted: {}", attachmentId);

        // 9. Retourner réponse
        AttachmentResponse response = attachmentMapper.toResponse(entity);
        response.setUrl(url);
        return response;
    }

    // GET BY ID
    @Override
    @Transactional
    public AttachmentResponse getById(UUID id) {
        Attachment entity = attachmentRepository.findByIdOptional(id)
                .orElseThrow(() -> new AttachmentNotFoundException(id));

        // Régénérer URL (Presigned URL expire après 15min)
        String url = minioClientService.getPresignedUrl(
                entity.getBucket(),
                entity.getMinioKey()
        );

        AttachmentResponse response = attachmentMapper.toResponse(entity);
        response.setUrl(url);
        return response;
    }

    // GET BY OWNER
    @Override
    @Transactional
    public List<AttachmentResponse> getByOwner(OwnerType ownerType,
                                               UUID ownerId) {
        return attachmentRepository.findByOwner(ownerType, ownerId)
                .stream()
                .map(entity -> {
                    String url = minioClientService.getPresignedUrl(
                            entity.getBucket(),
                            entity.getMinioKey()
                    );
                    AttachmentResponse response =
                            attachmentMapper.toResponse(entity);
                    response.setUrl(url);
                    return response;
                })
                .toList();
    }

    // DELETE
    @Override
    @Transactional
    public void delete(UUID id) {
        Attachment entity = attachmentRepository.findByIdOptional(id)
                .orElseThrow(() -> new AttachmentNotFoundException(id));

        // 1. Supprimer fichier depuis MinIO
        minioClientService.delete(
                entity.getBucket(),
                entity.getMinioKey()
        );
        log.info("File deleted from MinIO: {}", entity.getMinioKey());

        // 2. Supprimer métadonnées depuis PostgreSQL
        attachmentRepository.delete(entity);
        log.info("Metadata deleted: {}", id);
    }

    // Validation MIME
    private boolean isAllowedMimeType(String mimeType) {
        if (mimeType == null) return false;
        String mt = mimeType.toLowerCase();
        return mt.startsWith("image/")        ||
                mt.equals("application/pdf")   ||
                mt.startsWith("video/")        ||
                mt.contains("msword")          ||
                mt.contains("officedocument")  ||
                mt.contains("spreadsheet");
    }
}