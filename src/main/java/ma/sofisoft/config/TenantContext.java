package ma.sofisoft.config;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.exceptions.BusinessException;

@Slf4j
@RequestScoped
public class TenantContext {

    @Inject
    JsonWebToken jwt;

    public String getTenantId() {
        // Extraction sécurisée depuis le claim 'tenant' du JWT Keycloak
        String tenantId = jwt.getClaim("tenant");

        if (tenantId == null || tenantId.isBlank()) {
            log.error("Accès refusé : Aucun claim 'tenant' trouvé dans le JWT");
            throw new BusinessException("Unauthorized: Missing tenant claim", "TENANT_REQUIRED", 401);
        }
        return tenantId;
    }

    public String getBucket() {
        // Format : tenant-organization1
        return "tenant-" + getTenantId().toLowerCase();
    }

    public String buildMinioKey(String ownerType, String ownerId, String uuid, String extension) {
        // Format : photos/TIER/{uuid-tier}/{uuid-img}.jpg
        return String.format("%s/%s/%s.%s",
                ownerType, ownerId, uuid, extension.toLowerCase());
    }
}