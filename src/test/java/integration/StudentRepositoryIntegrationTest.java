package integration;

import entity.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;
import repository.StudentRepository;

import static org.assertj.core.api.Assertions.assertThat;

public class StudentRepositoryIntegrationTest {

    @Test
    void shouldPerformCompleteCrudForStudent() {

        EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("uniroom-test");

        EntityManager entityManager =
                entityManagerFactory.createEntityManager();

        StudentRepository repository =
                new StudentRepository(entityManager);

        try {

            // CREATE
            entityManager.getTransaction().begin();

            Student student = new Student(
                    "20260001",
                    "Daniel Hernandez",
                    "daniel.hernandez@uniroom.edu.co",
                    "hashed_password"
            );

            repository.save(student);

            entityManager.getTransaction().commit();

            Long studentId = student.getId();

            assertThat(studentId).isNotNull();

            // READ
            entityManager.clear();

            Student savedStudent =
                    repository.findById(studentId).orElseThrow();

            assertThat(savedStudent.getInstitutionalCode())
                    .isEqualTo("20260001");

            assertThat(savedStudent.getFullName())
                    .isEqualTo("Daniel Hernandez");

            // UPDATE
            entityManager.getTransaction().begin();

            savedStudent.setFullName("Daniel Hernandez Updated");

            repository.update(savedStudent);

            entityManager.getTransaction().commit();

            entityManager.clear();

            Student updatedStudent =
                    repository.findById(studentId).orElseThrow();

            assertThat(updatedStudent.getFullName())
                    .isEqualTo("Daniel Hernandez Updated");

            // DELETE
            entityManager.getTransaction().begin();

            repository.delete(updatedStudent);

            entityManager.getTransaction().commit();

            entityManager.clear();

            assertThat(repository.findById(studentId))
                    .isEmpty();

            System.out.println("Student CRUD integration test passed.");

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