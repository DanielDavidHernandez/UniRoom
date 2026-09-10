package integration;

import entity.Reservation;
import entity.Student;
import entity.StudyRoom;
import entity.TimeSlot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;
import service.ReservationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ReservationConcurrencyIntegrationTest {

    @Test
    void shouldAllowOnlyOneStudentToReserveSameTimeSlot()
            throws Exception {

        String password =
                System.getenv("UNIROOM_DB_PASSWORD");

        assertThat(password)
                .as("UNIROOM_DB_PASSWORD environment variable")
                .isNotBlank();

        Map<String, Object> properties =
                new HashMap<>();

        properties.put(
                "jakarta.persistence.jdbc.password",
                password
        );

        EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory(
                        "uniroom-mysql",
                        properties
                );

        Long studentAId = null;
        Long studentBId = null;
        Long roomId = null;
        Long timeSlotId = null;

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        try {

            // -------------------------------------------------
            // PREPARAR DATOS
            // -------------------------------------------------

            EntityManager setupManager =
                    entityManagerFactory.createEntityManager();

            setupManager.getTransaction().begin();

            String unique =
                    String.valueOf(System.currentTimeMillis());

            Student studentA = new Student(
                    "A" + unique,
                    "Concurrent Student A",
                    "studentA" + unique + "@uniroom.edu.co",
                    "hashed_password"
            );

            Student studentB = new Student(
                    "B" + unique,
                    "Concurrent Student B",
                    "studentB" + unique + "@uniroom.edu.co",
                    "hashed_password"
            );

            StudyRoom room = new StudyRoom(
                    "Concurrency Room " + unique,
                    "University Library",
                    6
            );

            setupManager.persist(studentA);
            setupManager.persist(studentB);
            setupManager.persist(room);

            TimeSlot timeSlot = new TimeSlot(
                    room,
                    LocalDateTime.of(
                            2026, 9, 20, 10, 0
                    ),
                    LocalDateTime.of(
                            2026, 9, 20, 11, 0
                    )
            );

            setupManager.persist(timeSlot);

            setupManager.getTransaction().commit();

            studentAId = studentA.getId();
            studentBId = studentB.getId();
            roomId = room.getId();
            timeSlotId = timeSlot.getId();

            setupManager.close();

            final Long finalStudentAId = studentAId;
            final Long finalStudentBId = studentBId;
            final Long finalTimeSlotId = timeSlotId;

            ReservationService reservationService =
                    new ReservationService(
                            entityManagerFactory
                    );

            // Los dos hilos esperarán aquí.
            CountDownLatch ready =
                    new CountDownLatch(2);

            CountDownLatch start =
                    new CountDownLatch(1);

            Callable<String> studentATask = () -> {

                ready.countDown();
                start.await();

                try {

                    Long reservationId =
                            reservationService
                                    .confirmReservation(
                                            finalStudentAId,
                                            finalTimeSlotId
                                    );

                    return "SUCCESS A - Reservation: "
                            + reservationId;

                } catch (Exception exception) {

                    return "FAILED A - "
                            + exception.getMessage();
                }
            };

            Callable<String> studentBTask = () -> {

                ready.countDown();
                start.await();

                try {

                    Long reservationId =
                            reservationService
                                    .confirmReservation(
                                            finalStudentBId,
                                            finalTimeSlotId
                                    );

                    return "SUCCESS B - Reservation: "
                            + reservationId;

                } catch (Exception exception) {

                    return "FAILED B - "
                            + exception.getMessage();
                }
            };

            Future<String> resultA =
                    executor.submit(studentATask);

            Future<String> resultB =
                    executor.submit(studentBTask);

            // Esperamos a que ambos hilos estén listos.
            ready.await();

            System.out.println(
                    "Both students are ready."
            );

            System.out.println(
                    "Concurrent reservation attempt started."
            );

            // Los soltamos prácticamente al mismo tiempo.
            start.countDown();

            String responseA =
                    resultA.get(
                            15,
                            TimeUnit.SECONDS
                    );

            String responseB =
                    resultB.get(
                            15,
                            TimeUnit.SECONDS
                    );

            System.out.println(responseA);
            System.out.println(responseB);

            long successfulReservations =
                    List.of(responseA, responseB)
                            .stream()
                            .filter(
                                    response ->
                                            response.startsWith(
                                                    "SUCCESS"
                                            )
                            )
                            .count();

            long failedReservations =
                    List.of(responseA, responseB)
                            .stream()
                            .filter(
                                    response ->
                                            response.startsWith(
                                                    "FAILED"
                                            )
                            )
                            .count();

            // Solamente uno debe conseguir la sala.
            assertThat(successfulReservations)
                    .isEqualTo(1);

            assertThat(failedReservations)
                    .isEqualTo(1);

            // -------------------------------------------------
            // VERIFICAR ESTADO REAL EN MYSQL
            // -------------------------------------------------

            EntityManager verificationManager =
                    entityManagerFactory
                            .createEntityManager();

            List<Reservation> reservations =
                    verificationManager
                            .createQuery(
                                    """
                                    SELECT r
                                    FROM Reservation r
                                    WHERE r.timeSlot.id = :timeSlotId
                                    """,
                                    Reservation.class
                            )
                            .setParameter(
                                    "timeSlotId",
                                    timeSlotId
                            )
                            .getResultList();

            assertThat(reservations)
                    .hasSize(1);

            assertThat(
                    reservations
                            .get(0)
                            .getStatus()
            ).isEqualTo("CONFIRMED");

            TimeSlot savedTimeSlot =
                    verificationManager.find(
                            TimeSlot.class,
                            timeSlotId
                    );

            assertThat(savedTimeSlot.getStatus())
                    .isEqualTo("RESERVED");

            verificationManager.close();

            System.out.println(
                    "Concurrency control test passed."
            );

            System.out.println(
                    "Confirmed reservations for the time slot: "
                            + reservations.size()
            );

            System.out.println(
                    "Final TimeSlot status: RESERVED"
            );

        } finally {

            executor.shutdownNow();

            // -------------------------------------------------
            // LIMPIAR DATOS DE LA PRUEBA
            // -------------------------------------------------

            if (entityManagerFactory.isOpen()) {

                EntityManager cleanupManager =
                        entityManagerFactory
                                .createEntityManager();

                try {

                    cleanupManager
                            .getTransaction()
                            .begin();

                    if (timeSlotId != null) {

                        cleanupManager
                                .createQuery(
                                        """
                                        DELETE FROM Reservation r
                                        WHERE r.timeSlot.id = :id
                                        """
                                )
                                .setParameter(
                                        "id",
                                        timeSlotId
                                )
                                .executeUpdate();

                        TimeSlot slot =
                                cleanupManager.find(
                                        TimeSlot.class,
                                        timeSlotId
                                );

                        if (slot != null) {
                            cleanupManager.remove(slot);
                        }
                    }

                    if (roomId != null) {

                        StudyRoom room =
                                cleanupManager.find(
                                        StudyRoom.class,
                                        roomId
                                );

                        if (room != null) {
                            cleanupManager.remove(room);
                        }
                    }

                    if (studentAId != null) {

                        Student student =
                                cleanupManager.find(
                                        Student.class,
                                        studentAId
                                );

                        if (student != null) {
                            cleanupManager.remove(student);
                        }
                    }

                    if (studentBId != null) {

                        Student student =
                                cleanupManager.find(
                                        Student.class,
                                        studentBId
                                );

                        if (student != null) {
                            cleanupManager.remove(student);
                        }
                    }

                    cleanupManager
                            .getTransaction()
                            .commit();

                } catch (Exception exception) {

                    if (cleanupManager
                            .getTransaction()
                            .isActive()) {

                        cleanupManager
                                .getTransaction()
                                .rollback();
                    }

                } finally {

                    cleanupManager.close();
                    entityManagerFactory.close();
                }
            }
        }
    }
}