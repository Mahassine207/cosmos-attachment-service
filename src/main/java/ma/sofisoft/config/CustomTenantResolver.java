package ma.sofisoft.config;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@PersistenceUnitExtension
public class CustomTenantResolver implements TenantResolver {

    @Inject
    TenantContext tenantContext;

    @Override
    public String getDefaultTenantId() {
        return "public"; // Schéma de secours
    }

    @Override
    public String resolveTenantId() {
        try {
            // Hibernate switch automatiquement sur le schéma du client extrait du JWT
            return tenantContext.getTenantId();
        } catch (Exception e) {
            return getDefaultTenantId();
        }
    }
}