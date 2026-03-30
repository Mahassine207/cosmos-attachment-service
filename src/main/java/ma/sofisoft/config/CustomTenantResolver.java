package ma.sofisoft.config;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@PersistenceUnitExtension
public class CustomTenantResolver implements TenantResolver {

    @Inject TenantContext tenantContext;
    @Inject DataSource dataSource;

    private final Set<String> validatedTenants = Collections.synchronizedSet(new HashSet<>());

    @Override
    public String getDefaultTenantId() {
        return "public";
    }

    @Override
    public String resolveTenantId() {
        String tenantId = tenantContext.getTenantId();
        if (tenantId == null || tenantId.isEmpty() || tenantId.equals("public")) {
            return "public";
        }

        // Nettoyage : PostgreSQL n'aime pas les tirets dans les noms de schémas non quotés
        // On remplace les tirets par des underscores
        String safeTenant = tenantId.replaceAll("-", "_").replaceAll("[^a-zA-Z0-9_]", "");

        ensureSchemaAndTablesExist(safeTenant);
        return safeTenant;
    }

    private void ensureSchemaAndTablesExist(String schema) {
        if (validatedTenants.contains(schema)) return;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // Création et clonage
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
            statement.execute("CREATE TABLE IF NOT EXISTS " + schema + ".attachment (LIKE public.attachment INCLUDING ALL)");
            statement.execute("CREATE TABLE IF NOT EXISTS " + schema + ".photos (LIKE public.photos INCLUDING ALL)");

            validatedTenants.add(schema);
            log.info("🚀 Schema architecture ready for tenant: {}", schema);

        } catch (Exception e) {
            log.error("❌ Critical error initializing tenant {}: {}", schema, e.getMessage());
        }
    }
}