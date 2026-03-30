package ma.sofisoft.config;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.exceptions.BusinessException;

@Slf4j
@RequestScoped
public class TenantContext {

    @Context
    HttpHeaders headers;

    public String getTenantId() {
        // Get the tenant from header X-Tenant-Id
        String tenantId = headers.getHeaderString("X-Tenant-Id");

        if (tenantId == null || tenantId.isBlank()) {
            log.error("Missing X-Tenant-Id header");
            throw new BusinessException(
                    "Missing X-Tenant-Id header",
                    "TENANT_REQUIRED",
                    400);
        }

        log.debug("TenantId resolved: {}", tenantId);
        return tenantId;
    }

    public String getBucket() {
        return "tenant-" + getTenantId().toLowerCase();
    }

    public String buildMinioKey(String ownerType,
                                String ownerId,
                                String uuid,
                                String extension) {
        return String.format("%s/%s/%s.%s",
                ownerType, ownerId, uuid, extension.toLowerCase());
    }
}