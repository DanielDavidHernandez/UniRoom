package repository;

import entity.Student;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class StudentRepository {

    private final EntityManager entityManager;

    public StudentRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Student save(Student student) {
        entityManager.persist(student);
        return student;
    }

    public Optional<Student> findById(Long id) {
        return Optional.ofNullable(
                entityManager.find(Student.class, id)
        );
    }

    public List<Student> findAll() {
        return entityManager
                .createQuery("SELECT s FROM Student s", Student.class)
                .getResultList();
    }

    public Student update(Student student) {
        return entityManager.merge(student);
    }

    public void delete(Student student) {
        entityManager.remove(
                entityManager.contains(student)
                        ? student
                        : entityManager.merge(student)
        );
    }

    public Optional<Student> findByInstitutionalCode(String institutionalCode) {
        return entityManager
                .createQuery(
                        "SELECT s FROM Student s WHERE s.institutionalCode = :code",
                        Student.class
                )
                .setParameter("code", institutionalCode)
                .getResultStream()
                .findFirst();
    }
}