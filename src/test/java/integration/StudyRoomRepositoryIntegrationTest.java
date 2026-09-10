package integration;

import entity.StudyRoom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;
import repository.StudyRoomRepository;

import static org.assertj.core.api.Assertions.assertThat;

public class StudyRoomRepositoryIntegrationTest {

    @Test
    void shouldPerformCompleteCrudForStudyRoom() {

        EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("uniroom-test");

        EntityManager entityManager =
                entityManagerFactory.createEntityManager();

        StudyRoomRepository repository =
                new StudyRoomRepository(entityManager);

        try {

            // CREATE
            entityManager.getTransaction().begin();

            StudyRoom room = new StudyRoom(
                    "Room 101",
                    "Library - Floor 2",
                    6
            );

            repository.save(room);

            entityManager.getTransaction().commit();

            Long roomId = room.getId();

            assertThat(roomId).isNotNull();

            // READ
            entityManager.clear();

            StudyRoom savedRoom =
                    repository.findById(roomId).orElseThrow();

            assertThat(savedRoom.getName())
                    .isEqualTo("Room 101");

            assertThat(savedRoom.getCapacity())
                    .isEqualTo(6);

            // UPDATE
            entityManager.getTransaction().begin();

            savedRoom.setCapacity(8);

            repository.update(savedRoom);

            entityManager.getTransaction().commit();

            entityManager.clear();

            StudyRoom updatedRoom =
                    repository.findById(roomId).orElseThrow();

            assertThat(updatedRoom.getCapacity())
                    .isEqualTo(8);

            // DELETE
            entityManager.getTransaction().begin();

            repository.delete(updatedRoom);

            entityManager.getTransaction().commit();

            entityManager.clear();

            assertThat(repository.findById(roomId))
                    .isEmpty();

            System.out.println("StudyRoom CRUD integration test passed.");

        } finally {

            if (entityManager.isOpen()) {
                entityManager.close();
            }

            if (entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
            }
        }
    }
}