package repository;

import entity.StudyRoom;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class StudyRoomRepository {

    private final EntityManager entityManager;

    public StudyRoomRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public StudyRoom save(StudyRoom studyRoom) {
        entityManager.persist(studyRoom);
        return studyRoom;
    }

    public Optional<StudyRoom> findById(Long id) {
        return Optional.ofNullable(
                entityManager.find(StudyRoom.class, id)
        );
    }

    public List<StudyRoom> findAll() {
        return entityManager
                .createQuery("SELECT r FROM StudyRoom r", StudyRoom.class)
                .getResultList();
    }

    public StudyRoom update(StudyRoom studyRoom) {
        return entityManager.merge(studyRoom);
    }

    public void delete(StudyRoom studyRoom) {
        entityManager.remove(
                entityManager.contains(studyRoom)
                        ? studyRoom
                        : entityManager.merge(studyRoom)
        );
    }

    public List<StudyRoom> findActiveRooms() {
        return entityManager
                .createQuery(
                        "SELECT r FROM StudyRoom r WHERE r.isActive = true",
                        StudyRoom.class
                )
                .getResultList();
    }
}