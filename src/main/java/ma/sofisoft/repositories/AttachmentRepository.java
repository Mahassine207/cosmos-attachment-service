package ma.sofisoft.repositories;

import ma.sofisoft.entities.Attachment;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import ma.sofisoft.enums.OwnerType;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AttachmentRepository
        implements PanacheRepositoryBase<Attachment, UUID> {

    public List<Attachment> findByOwner(OwnerType ownerType,
                                        UUID ownerId) {
        return find("ownerType = ?1 and ownerId = ?2",
                ownerType, ownerId).list();
    }

    public List<Attachment> findByMimeType(OwnerType ownerType,
                                           UUID ownerId,
                                           String mimeType) {
        return find("ownerType = ?1 and ownerId = ?2 " +
                        "and mimeType = ?3",
                ownerType, ownerId, mimeType).list();
    }
}
