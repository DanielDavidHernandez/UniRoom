package service;

import config.JpaUtil;
import entity.Student;
import entity.TimeSlot;
import entity.WaitlistEntry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import repository.ReservationRepository;
import repository.TimeSlotRepository;
import repository.WaitlistRepository;

public class WaitlistService {

    private final EntityManagerFactory entityManagerFactory;

    public WaitlistService() {
        this.entityManagerFactory =
                JpaUtil.getEntityManagerFactory();
    }

    public WaitlistService(
            EntityManagerFactory entityManagerFactory) {

        this.entityManagerFactory =
                entityManagerFactory;
    }

    public int joinWaitlist(
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

            WaitlistRepository waitlistRepository =
                    new WaitlistRepository(entityManager);

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

            if ("AVAILABLE".equals(timeSlot.getStatus())) {

                throw new IllegalStateException(
                        "Este horario está disponible. Puedes reservarlo directamente."
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
                        "Ya tienes una reserva confirmada para este horario."
                );
            }

            if (waitlistRepository.existsWaiting(
                    studentId,
                    timeSlotId)) {

                throw new IllegalStateException(
                        "Ya estás en la lista de espera de este horario."
                );
            }

            int position =
                    waitlistRepository.nextPosition(
                            timeSlotId
                    );

            WaitlistEntry entry =
                    new WaitlistEntry();

            entry.setStudent(student);
            entry.setTimeSlot(timeSlot);
            entry.setPosition(position);
            entry.setStatus("WAITING");

            waitlistRepository.save(entry);

            transaction.commit();

            return position;

        } catch (RuntimeException exception) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw exception;

        } finally {
            entityManager.close();
        }
    }

    public void leaveWaitlist(
            Long waitlistId,
            Long studentId) {

        EntityManager entityManager =
                entityManagerFactory.createEntityManager();

        EntityTransaction transaction =
                entityManager.getTransaction();

        try {

            transaction.begin();

            WaitlistRepository waitlistRepository =
                    new WaitlistRepository(entityManager);

            WaitlistEntry entry =
                    entityManager.find(
                            WaitlistEntry.class,
                            waitlistId
                    );

            if (entry == null) {

                throw new IllegalArgumentException(
                        "La entrada de lista de espera no existe."
                );
            }

            if (!entry.getStudent()
                    .getId()
                    .equals(studentId)) {

                throw new IllegalStateException(
                        "No puedes modificar la lista de espera de otro estudiante."
                );
            }

            if (!"WAITING".equals(entry.getStatus())) {

                throw new IllegalStateException(
                        "Esta entrada ya no está activa."
                );
            }

            Long timeSlotId =
                    entry.getTimeSlot().getId();

            entry.setStatus("CANCELLED");

            entityManager.merge(entry);

            waitlistRepository.normalizePositions(
                    timeSlotId
            );

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