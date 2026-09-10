package service;

import config.JpaUtil;
import entity.Reservation;
import entity.Student;
import entity.TimeSlot;
import entity.WaitlistEntry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import repository.ReservationRepository;
import repository.TimeSlotRepository;
import repository.WaitlistRepository;

import java.time.LocalDateTime;
import java.util.Optional;

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

        EntityTransaction transaction =
                entityManager.getTransaction();

        try {

            transaction.begin();

            TimeSlotRepository timeSlotRepository =
                    new TimeSlotRepository(entityManager);

            ReservationRepository reservationRepository =
                    new ReservationRepository(entityManager);

            TimeSlot timeSlot =
                    timeSlotRepository
                            .findByIdForUpdate(timeSlotId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "El horario no existe."
                                    )
                            );

            if (!"AVAILABLE".equals(
                    timeSlot.getStatus())) {

                throw new IllegalStateException(
                        "El horario ya no está disponible."
                );
            }

            Student student =
                    entityManager.find(
                            Student.class,
                            studentId
                    );

            if (student == null) {

                throw new IllegalArgumentException(
                        "El estudiante no existe."
                );
            }

            if (reservationRepository
                    .existsConfirmedByStudentAndTimeSlot(
                            studentId,
                            timeSlotId)) {

                throw new IllegalStateException(
                        "Ya tienes una reserva para este horario."
                );
            }

            Reservation reservation =
                    new Reservation();

            reservation.setStudent(student);
            reservation.setTimeSlot(timeSlot);
            reservation.setStatus("CONFIRMED");
            reservation.setConfirmedAt(
                    LocalDateTime.now()
            );

            reservationRepository.save(
                    reservation
            );

            timeSlot.setStatus(
                    "RESERVED"
            );

            timeSlotRepository.update(
                    timeSlot
            );

            transaction.commit();

            return reservation.getId();

        } catch (RuntimeException exception) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw exception;

        } finally {
            entityManager.close();
        }
    }

    public void cancelReservation(
            Long reservationId,
            Long studentId) {

        EntityManager entityManager =
                entityManagerFactory.createEntityManager();

        EntityTransaction transaction =
                entityManager.getTransaction();

        try {

            transaction.begin();

            ReservationRepository reservationRepository =
                    new ReservationRepository(entityManager);

            TimeSlotRepository timeSlotRepository =
                    new TimeSlotRepository(entityManager);

            WaitlistRepository waitlistRepository =
                    new WaitlistRepository(entityManager);

            Reservation reservation =
                    entityManager.find(
                            Reservation.class,
                            reservationId
                    );

            if (reservation == null) {

                throw new IllegalArgumentException(
                        "La reserva no existe."
                );
            }

            if (!reservation
                    .getStudent()
                    .getId()
                    .equals(studentId)) {

                throw new IllegalStateException(
                        "No puedes cancelar una reserva de otro estudiante."
                );
            }

            if (!"CONFIRMED".equals(
                    reservation.getStatus())) {

                throw new IllegalStateException(
                        "La reserva ya no está activa."
                );
            }

            Long timeSlotId =
                    reservation
                            .getTimeSlot()
                            .getId();

            TimeSlot timeSlot =
                    timeSlotRepository
                            .findByIdForUpdate(timeSlotId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "El horario no existe."
                                    )
                            );

            reservation.setStatus(
                    "CANCELLED"
            );

            entityManager.merge(
                    reservation
            );

            Optional<WaitlistEntry> nextWaiting =
                    waitlistRepository
                            .findNextWaitingForUpdate(
                                    timeSlotId
                            );

            if (nextWaiting.isPresent()) {

                WaitlistEntry entry =
                        nextWaiting.get();

                Reservation newReservation =
                        new Reservation();

                newReservation.setStudent(
                        entry.getStudent()
                );

                newReservation.setTimeSlot(
                        timeSlot
                );

                newReservation.setStatus(
                        "CONFIRMED"
                );

                newReservation.setConfirmedAt(
                        LocalDateTime.now()
                );

                reservationRepository.save(
                        newReservation
                );

                entry.setStatus(
                        "ASSIGNED"
                );

                entityManager.merge(
                        entry
                );

                timeSlot.setStatus(
                        "RESERVED"
                );

                entityManager.merge(
                        timeSlot
                );

                waitlistRepository.normalizePositions(
                        timeSlotId
                );

            } else {

                timeSlot.setStatus(
                        "AVAILABLE"
                );

                entityManager.merge(
                        timeSlot
                );
            }

            transaction.commit();

        } catch (RuntimeException exception) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw exception;

        } finally {
            entityManager.close();
        }
    }
}