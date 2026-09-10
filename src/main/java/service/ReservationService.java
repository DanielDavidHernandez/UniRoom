package service;

import config.JpaUtil;
import entity.Reservation;
import entity.Student;
import entity.TimeSlot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import repository.ReservationRepository;
import repository.TimeSlotRepository;

import java.time.LocalDateTime;

public class ReservationService {

    private final EntityManagerFactory entityManagerFactory;

    public ReservationService() {
        this.entityManagerFactory =
                JpaUtil.getEntityManagerFactory();
    }

    public ReservationService(
            EntityManagerFactory entityManagerFactory) {

        this.entityManagerFactory =
                entityManagerFactory;
    }

    public Long confirmReservation(
            Long studentId,
            Long timeSlotId) {

        EntityManager entityManager =
                entityManagerFactory.createEntityManager();

        try {

            entityManager.getTransaction().begin();

            TimeSlotRepository timeSlotRepository =
                    new TimeSlotRepository(entityManager);

            ReservationRepository reservationRepository =
                    new ReservationRepository(entityManager);

            // Bloqueo pesimista del horario solicitado.
            TimeSlot timeSlot =
                    timeSlotRepository
                            .findByIdForUpdate(timeSlotId)
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Time slot not found."
                                    )
                            );

            // Después de obtener el bloqueo,
            // se comprueba nuevamente su disponibilidad.
            if (!"AVAILABLE".equals(timeSlot.getStatus())) {

                throw new IllegalStateException(
                        "The selected time slot is no longer available."
                );
            }

            Student student =
                    entityManager.find(
                            Student.class,
                            studentId
                    );

            if (student == null) {
                throw new IllegalArgumentException(
                        "Student not found."
                );
            }

            // Crear y confirmar la reserva.
            Reservation reservation =
                    new Reservation(
                            student,
                            timeSlot
                    );

            reservation.setStatus("CONFIRMED");
            reservation.setConfirmedAt(
                    LocalDateTime.now()
            );

            reservationRepository.save(
                    reservation
            );

            // La franja deja de estar disponible.
            timeSlot.setStatus("RESERVED");

            entityManager.getTransaction().commit();

            return reservation.getId();

        } catch (RuntimeException exception) {

            if (entityManager
                    .getTransaction()
                    .isActive()) {

                entityManager
                        .getTransaction()
                        .rollback();
            }

            throw exception;

        } finally {

            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }
}