package ma.sofisoft.repositories;

import ma.sofisoft.entities.Photo;
import ma.sofisoft.enums.OwnerType;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PhotoRepository implements PanacheRepositoryBase<Photo, UUID> {

    public List<Photo> findByOwner(OwnerType ownerType,
                                   UUID ownerId) {
        return find("ownerType = ?1 and ownerId = ?2",
                ownerType, ownerId).list();
    }

    public List<Photo> findMainByOwner(OwnerType ownerType,
                                       UUID ownerId) {
        return find("ownerType = ?1 and ownerId = ?2 " +
                        "and isMain = true",
                ownerType, ownerId).list();
    }
}