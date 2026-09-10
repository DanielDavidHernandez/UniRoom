package config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

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

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                "jakarta.persistence.jdbc.password",
                password
        );

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