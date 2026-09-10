package repository;

import entity.Reservation;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class ReservationRepository {

    private final EntityManager entityManager;

    public ReservationRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Reservation save(Reservation reservation) {
        entityManager.persist(reservation);
        return reservation;
    }

    public Optional<Reservation> findById(Long id) {
        return Optional.ofNullable(
                entityManager.find(Reservation.class, id)
        );
    }

    public List<Reservation> findAll() {
        return entityManager
                .createQuery(
                        "SELECT r FROM Reservation r",
                        Reservation.class
                )
                .getResultList();
    }

    public Reservation update(Reservation reservation) {
        return entityManager.merge(reservation);
    }

    public void delete(Reservation reservation) {
        entityManager.remove(
                entityManager.contains(reservation)
                        ? reservation
                        : entityManager.merge(reservation)
        );
    }

    public List<Reservation> findAllWithDetails() {
        return entityManager
                .createQuery(
                        """
                        SELECT DISTINCT r
                        FROM Reservation r
                        JOIN FETCH r.student
                        JOIN FETCH r.timeSlot ts
                        JOIN FETCH ts.studyRoom
                        """,
                        Reservation.class
                )
                .getResultList();
    }
}