package repository;

import entity.WaitlistEntry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public class WaitlistRepository {

    private final EntityManager entityManager;

    public WaitlistRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public WaitlistEntry save(WaitlistEntry entry) {
        entityManager.persist(entry);
        return entry;
    }

    public Optional<WaitlistEntry> findById(Long id) {
        return Optional.ofNullable(
                entityManager.find(WaitlistEntry.class, id)
        );
    }

    public List<WaitlistEntry> findAll() {
        return entityManager
                .createQuery(
                        """
                        SELECT w
                        FROM WaitlistEntry w
                        ORDER BY w.createdAt
                        """,
                        WaitlistEntry.class
                )
                .getResultList();
    }

    public WaitlistEntry update(WaitlistEntry entry) {
        return entityManager.merge(entry);
    }

    public boolean existsWaiting(
            Long studentId,
            Long timeSlotId) {

        Long count = entityManager
                .createQuery(
                        """
                        SELECT COUNT(w)
                        FROM WaitlistEntry w
                        WHERE w.student.id = :studentId
                        AND w.timeSlot.id = :timeSlotId
                        AND w.status = 'WAITING'
                        """,
                        Long.class
                )
                .setParameter("studentId", studentId)
                .setParameter("timeSlotId", timeSlotId)
                .getSingleResult();

        return count > 0;
    }

    public int nextPosition(Long timeSlotId) {

        Integer maximum = entityManager
                .createQuery(
                        """
                        SELECT MAX(w.position)
                        FROM WaitlistEntry w
                        WHERE w.timeSlot.id = :timeSlotId
                        AND w.status = 'WAITING'
                        """,
                        Integer.class
                )
                .setParameter("timeSlotId", timeSlotId)
                .getSingleResult();

        return maximum == null ? 1 : maximum + 1;
    }

    public Optional<WaitlistEntry> findNextWaitingForUpdate(
            Long timeSlotId) {

        return entityManager
                .createQuery(
                        """
                        SELECT w
                        FROM WaitlistEntry w
                        JOIN FETCH w.student
                        JOIN FETCH w.timeSlot
                        WHERE w.timeSlot.id = :timeSlotId
                        AND w.status = 'WAITING'
                        ORDER BY w.position ASC
                        """,
                        WaitlistEntry.class
                )
                .setParameter("timeSlotId", timeSlotId)
                .setMaxResults(1)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream()
                .findFirst();
    }

    public List<WaitlistEntry> findWaitingByStudentIdWithDetails(
            Long studentId) {

        return entityManager
                .createQuery(
                        """
                        SELECT w
                        FROM WaitlistEntry w
                        JOIN FETCH w.student
                        JOIN FETCH w.timeSlot ts
                        JOIN FETCH ts.studyRoom
                        WHERE w.student.id = :studentId
                        AND w.status = 'WAITING'
                        ORDER BY ts.startTime
                        """,
                        WaitlistEntry.class
                )
                .setParameter("studentId", studentId)
                .getResultList();
    }

    public List<WaitlistEntry> findWaitingByTimeSlotId(
            Long timeSlotId) {

        return entityManager
                .createQuery(
                        """
                        SELECT w
                        FROM WaitlistEntry w
                        JOIN FETCH w.student
                        WHERE w.timeSlot.id = :timeSlotId
                        AND w.status = 'WAITING'
                        ORDER BY w.position
                        """,
                        WaitlistEntry.class
                )
                .setParameter("timeSlotId", timeSlotId)
                .getResultList();
    }

    public void normalizePositions(Long timeSlotId) {

        List<WaitlistEntry> entries =
                findWaitingByTimeSlotId(timeSlotId);

        int position = 1;

        for (WaitlistEntry entry : entries) {
            entry.setPosition(position++);
            entityManager.merge(entry);
        }
    }
}