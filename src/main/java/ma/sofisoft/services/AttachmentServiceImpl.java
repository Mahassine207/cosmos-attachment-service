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
import ma.sofisoft.entities.Photo;
import ma.sofisoft.enums.OwnerType;
import ma.sofisoft.exceptions.AttachmentNotFoundException;
import ma.sofisoft.exceptions.BusinessException;
import ma.sofisoft.mappers.AttachmentMapper;
import ma.sofisoft.repositories.AttachmentRepository;
import ma.sofisoft.repositories.PhotoRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class AttachmentServiceImpl implements AttachmentService {

    @Inject
    AttachmentRepository attachmentRepository;

    @Inject
    PhotoRepository photoRepository;

    @Inject
    MinioClientService minioClientService;

    @Inject
    AttachmentMapper attachmentMapper;

    @Inject
    TenantContext tenantContext;

    private static final long MAX_SIZE = 20L * 1024 * 1024; // 20MB

    // ==========================================
    //                UPLOAD
    // ==========================================
    @Override
    @Transactional
    public AttachmentResponse upload(OwnerType ownerType, UUID ownerId, CreateAttachmentRequest request) {
        // 1. Validation de sécurité
        validateRequest(request);

        String mimeType = request.getFile().contentType();
        String filename = request.getFile().fileName();
        String extension = getExtension(filename);
        String user = (request.getCreatedBy() != null) ? request.getCreatedBy() : "anonymous";

        // 2. Détection du type (Photo vs Document)
        boolean isPhoto = (mimeType != null && mimeType.toLowerCase().startsWith("image/"));

        // 3. Préparation du stockage MinIO
        // On génère un UUID unique pour le nom du fichier sur S3 (différent de l'ID DB)
        UUID storageUuid = UUID.randomUUID();
        String bucket = tenantContext.getBucket();
        String minioKey = tenantContext.buildMinioKey(
                ownerType.name(),
                ownerId.toString(),
                storageUuid.toString(),
                extension
        );

        // 4. Upload physique vers MinIO
        log.info("📤 Uploading {} to MinIO (Key: {})", isPhoto ? "PHOTO" : "FILE", minioKey);
        minioClientService.upload(bucket, minioKey, request.getFile().uploadedFile(), mimeType);

        // 5. Génération de l'URL d'accès
        String url = minioClientService.getPresignedUrl(bucket, minioKey);

        // 6. Persistance en base de données
        if (isPhoto) {
            return persistAsPhoto(ownerType, ownerId, url, user, filename, mimeType);
        } else {
            return persistAsAttachment(ownerType, ownerId, bucket, minioKey, url, user, filename, mimeType, request.getFile().size());
        }
    }

    private AttachmentResponse persistAsPhoto(OwnerType type, UUID ownerId, String url, String user, String name, String mime) {
        long photoCount = photoRepository.count("ownerType = ?1 and ownerId = ?2", type, ownerId);

        Photo photo = Photo.builder()
                .ownerType(type)
                .ownerId(ownerId)
                .url(url)
                .isMain(photoCount == 0)
                .displayOrder((int) photoCount)
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .build();

        // Hibernate génère l'ID ici
        photoRepository.persist(photo);
        log.info("✅ Photo saved with DB-ID: {}", photo.getId());

        return buildResponseFromPhoto(photo, name, mime);
    }

    private AttachmentResponse persistAsAttachment(OwnerType type, UUID ownerId, String bucket, String key, String url, String user, String name, String mime, long size) {
        Attachment entity = Attachment.builder()
                // On ne passe PAS d'ID manuel ici pour éviter l'EntityExistsException
                .ownerType(type)
                .ownerId(ownerId)
                .originalFilename(name)
                .mimeType(mime)
                .sizeBytes(size)
                .minioKey(key)
                .bucket(bucket)
                .url(url)
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .build();

        // Hibernate génère l'ID ici
        attachmentRepository.persist(entity);
        log.info("✅ Attachment saved with DB-ID: {}", entity.getId());

        AttachmentResponse response = attachmentMapper.toResponse(entity);
        response.setUrl(url);
        return response;
    }

    // ==========================================
    //              GET BY ID
    // ==========================================
    @Override
    @Transactional
    public AttachmentResponse getById(UUID id) {
        return attachmentRepository.findByIdOptional(id)
                .map(e -> {
                    AttachmentResponse res = attachmentMapper.toResponse(e);
                    res.setUrl(minioClientService.getPresignedUrl(e.getBucket(), e.getMinioKey()));
                    return res;
                })
                .orElseGet(() -> photoRepository.findByIdOptional(id)
                        .map(p -> buildResponseFromPhoto(p, "Image", "image/*"))
                        .orElseThrow(() -> new AttachmentNotFoundException(id)));
    }

    // ==========================================
    //             GET BY OWNER
    // ==========================================
    @Override
    @Transactional
    public List<AttachmentResponse> getByOwner(OwnerType ownerType, UUID ownerId) {
        List<AttachmentResponse> results = attachmentRepository.findByOwner(ownerType, ownerId)
                .stream()
                .map(e -> {
                    AttachmentResponse res = attachmentMapper.toResponse(e);
                    res.setUrl(minioClientService.getPresignedUrl(e.getBucket(), e.getMinioKey()));
                    return res;
                })
                .collect(Collectors.toCollection(ArrayList::new));

        photoRepository.find("ownerType = ?1 and ownerId = ?2", ownerType, ownerId)
                .list()
                .forEach(p -> results.add(buildResponseFromPhoto(p, "Image", "image/*")));

        return results;
    }

    // ==========================================
    //               DELETE
    // ==========================================
    @Override
    @Transactional
    public void delete(UUID id) {
        // 1. Chercher dans les Attachments (Documents)
        var attachmentOpt = attachmentRepository.findByIdOptional(id);
        if (attachmentOpt.isPresent()) {
            Attachment e = attachmentOpt.get();
            // Suppression physique sur MinIO
            minioClientService.delete(e.getBucket(), e.getMinioKey());
            // Suppression DB
            attachmentRepository.delete(e);
            log.info("🗑️ Document deleted (DB + MinIO): {}", id);
            return;
        }

        // 2. Chercher dans les Photos
        var photoOpt = photoRepository.findByIdOptional(id);
        if (photoOpt.isPresent()) {
            Photo p = photoOpt.get();
            // Note: On pourrait aussi supprimer sur MinIO ici si on stockait la clé
            photoRepository.delete(p);
            log.info("🗑️ Photo deleted (DB): {}", id);
            return;
        }

        throw new AttachmentNotFoundException(id);
    }

    // ==========================================
    //              HELPERS
    // ==========================================
    private void validateRequest(CreateAttachmentRequest request) {
        if (request.getFile() == null || request.getFile().uploadedFile() == null) {
            throw new BusinessException("Missing file", "MISSING_FILE", 400);
        }
        if (request.getFile().size() > MAX_SIZE) {
            throw new BusinessException("File too large (max 20MB)", "FILE_TOO_LARGE", 413);
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0) ? filename.substring(dotIndex + 1).toLowerCase() : "bin";
    }

    private AttachmentResponse buildResponseFromPhoto(Photo p, String name, String mime) {
        AttachmentResponse res = new AttachmentResponse();
        res.setId(p.getId());
        res.setOwnerType(p.getOwnerType());
        res.setOwnerId(p.getOwnerId());
        res.setUrl(p.getUrl());
        res.setMimeType(mime);
        res.setOriginalFilename(name);
        res.setCreatedBy(p.getCreatedBy());
        res.setCreatedAt(p.getCreatedAt());
        return res;
    }
}