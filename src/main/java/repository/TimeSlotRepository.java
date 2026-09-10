package repository;

import entity.TimeSlot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public class TimeSlotRepository {

    private final EntityManager entityManager;

    public TimeSlotRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public TimeSlot save(TimeSlot timeSlot) {
        entityManager.persist(timeSlot);
        return timeSlot;
    }

    public Optional<TimeSlot> findById(Long id) {
        return Optional.ofNullable(
                entityManager.find(TimeSlot.class, id)
        );
    }

    public List<TimeSlot> findAll() {
        return entityManager
                .createQuery(
                        "SELECT ts FROM TimeSlot ts",
                        TimeSlot.class
                )
                .getResultList();
    }

    public TimeSlot update(TimeSlot timeSlot) {
        return entityManager.merge(timeSlot);
    }

    public void delete(TimeSlot timeSlot) {
        entityManager.remove(
                entityManager.contains(timeSlot)
                        ? timeSlot
                        : entityManager.merge(timeSlot)
        );
    }

    public Optional<TimeSlot> findByIdForUpdate(Long id) {
        return entityManager
                .createQuery(
                        """
                        SELECT ts
                        FROM TimeSlot ts
                        WHERE ts.id = :id
                        """,
                        TimeSlot.class
                )
                .setParameter("id", id)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream()
                .findFirst();
    }

    public List<TimeSlot> findAvailableSlots() {
        return entityManager
                .createQuery(
                        """
                        SELECT ts
                        FROM TimeSlot ts
                        JOIN FETCH ts.studyRoom
                        WHERE ts.status = 'AVAILABLE'
                        ORDER BY ts.startTime
                        """,
                        TimeSlot.class
                )
                .getResultList();
    }
}