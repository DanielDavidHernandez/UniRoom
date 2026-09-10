package integration;

import entity.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MySqlOrmIntegrationTest {

    @Test
    void shouldPersistAndReadStudentUsingRealMySql() {

        String password = System.getenv("UNIROOM_DB_PASSWORD");

        assertThat(password)
                .as("UNIROOM_DB_PASSWORD environment variable")
                .isNotBlank();

        Map<String, Object> properties = new HashMap<>();
        properties.put(
                "jakarta.persistence.jdbc.password",
                password
        );

        EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory(
                        "uniroom-mysql",
                        properties
                );

        EntityManager entityManager =
                entityManagerFactory.createEntityManager();

        Long studentId = null;

        try {

            String uniqueValue =
                    String.valueOf(System.currentTimeMillis());

            // CREATE
            entityManager.getTransaction().begin();

            Student student = new Student(
                    "ST" + uniqueValue,
                    "MySQL Integration Student",
                    "student" + uniqueValue + "@uniroom.edu.co",
                    "hashed_password"
            );

            entityManager.persist(student);

            entityManager.getTransaction().commit();

            studentId = student.getId();

            assertThat(studentId).isNotNull();

            // READ
            entityManager.clear();

            Student savedStudent =
                    entityManager.find(Student.class, studentId);

            assertThat(savedStudent).isNotNull();

            assertThat(savedStudent.getFullName())
                    .isEqualTo("MySQL Integration Student");

            System.out.println(
                    "Student successfully persisted in real MySQL."
            );

            System.out.println(
                    "Generated student ID: " + studentId
            );

            // CLEANUP
            entityManager.getTransaction().begin();

            Student studentToDelete =
                    entityManager.find(Student.class, studentId);

            entityManager.remove(studentToDelete);

            entityManager.getTransaction().commit();

            entityManager.clear();

            assertThat(
                    entityManager.find(Student.class, studentId)
            ).isNull();

            System.out.println(
                    "Test data successfully removed from MySQL."
            );

        } finally {

            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }

            if (entityManager.isOpen()) {
                entityManager.close();
            }

            if (entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
            }
        }
    }
}