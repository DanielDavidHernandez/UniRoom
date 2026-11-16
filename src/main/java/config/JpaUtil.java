package config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.flywaydb.core.Flyway;

import java.util.HashMap;
import java.util.Map;

public final class JpaUtil {

    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY =
            createEntityManagerFactory();

    private JpaUtil() {
    }

    private static EntityManagerFactory createEntityManagerFactory() {

        String password =
                System.getenv("UNIROOM_DB_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Environment variable UNIROOM_DB_PASSWORD is not configured."
            );
        }

        // Local settings remain unchanged unless Docker/Railway supplies variables.
        String jdbcUrl = System.getenv("UNIROOM_DB_URL");
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            jdbcUrl = "jdbc:mysql://localhost:3306/uniroom_db"
                    + "?useSSL=false&allowPublicKeyRetrieval=true"
                    + "&connectionTimeZone=America/Bogota";
        }
        String dbUser = System.getenv("UNIROOM_DB_USER");
        if (dbUser == null || dbUser.isBlank()) {
            dbUser = "root";
        }

        // Fresh cloud databases require the same Flyway migrations as local CI.
        Flyway.configure()
                .dataSource(jdbcUrl, dbUser, password)
                .locations("classpath:db/migration")
                .load()
                .migrate();

        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
        properties.put("jakarta.persistence.jdbc.user", dbUser);
        properties.put("jakarta.persistence.jdbc.password", password);

        return Persistence.createEntityManagerFactory(
                "uniroom-mysql",
                properties
        );
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return ENTITY_MANAGER_FACTORY;
    }

    public static void shutdown() {
        if (ENTITY_MANAGER_FACTORY.isOpen()) {
            ENTITY_MANAGER_FACTORY.close();
        }
    }
}