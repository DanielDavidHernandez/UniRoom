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

    // Crear un horario
    public TimeSlot save(TimeSlot timeSlot) {
        entityManager.persist(timeSlot);
        return timeSlot;
    }

    // Buscar horario por ID
    public Optional<TimeSlot> findById(Long id) {
        return Optional.ofNullable(
                entityManager.find(TimeSlot.class, id)
        );
    }

    // Obtener todos los horarios
    public List<TimeSlot> findAll() {
        return entityManager
                .createQuery(
                        "SELECT ts FROM TimeSlot ts",
                        TimeSlot.class
                )
                .getResultList();
    }

    // Actualizar horario
    public TimeSlot update(TimeSlot timeSlot) {
        return entityManager.merge(timeSlot);
    }

    // Eliminar horario
    public void delete(TimeSlot timeSlot) {
        entityManager.remove(
                entityManager.contains(timeSlot)
                        ? timeSlot
                        : entityManager.merge(timeSlot)
        );
    }

    // Buscar horario y bloquearlo para evitar doble reserva
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

    // Obtener únicamente horarios disponibles
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

    // Obtener todos los horarios correspondientes a una sala
    public List<TimeSlot> findByRoomId(Long roomId) {
        return entityManager
                .createQuery(
                        """
                        SELECT ts
                        FROM TimeSlot ts
                        JOIN FETCH ts.studyRoom
                        WHERE ts.studyRoom.id = :roomId
                        ORDER BY ts.startTime
                        """,
                        TimeSlot.class
                )
                .setParameter("roomId", roomId)
                .getResultList();
    }
}