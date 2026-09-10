package integration;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HibernateConnectionTest {

    @Test
    void shouldStartHibernateWithUniRoomEntities() {

        EntityManagerFactory entityManagerFactory = null;

        try {
            entityManagerFactory =
                    Persistence.createEntityManagerFactory("uniroom-test");

            assertThat(entityManagerFactory).isNotNull();
            assertThat(entityManagerFactory.isOpen()).isTrue();

            System.out.println("Hibernate started successfully.");
            System.out.println("UniRoom ORM configuration is working.");

        } finally {
            if (entityManagerFactory != null
                    && entityManagerFactory.isOpen()) {

                entityManagerFactory.close();
            }
        }
    }
}