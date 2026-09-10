package integration;

import entity.Reservation;
import entity.Student;
import entity.StudyRoom;
import entity.TimeSlot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;
import repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ReservationRepositoryIntegrationTest {

    @Test
    void shouldPerformCrudAndJoinFetchForReservation() {

        EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("uniroom-test");

        EntityManager entityManager =
                entityManagerFactory.createEntityManager();

        ReservationRepository repository =
                new ReservationRepository(entityManager);

        try {

            // CREATE RELATED ENTITIES
            entityManager.getTransaction().begin();

            Student student = new Student(
                    "20260002",
                    "Juan Diego Bedoya",
                    "juan.bedoya@uniroom.edu.co",
                    "hashed_password"
            );

            StudyRoom room = new StudyRoom(
                    "Room 202",
                    "Library - Floor 3",
                    8
            );

            entityManager.persist(student);
            entityManager.persist(room);

            TimeSlot timeSlot = new TimeSlot(
                    room,
                    LocalDateTime.of(2026, 9, 15, 10, 0),
                    LocalDateTime.of(2026, 9, 15, 11, 0)
            );

            entityManager.persist(timeSlot);

            Reservation reservation =
                    new Reservation(student, timeSlot);

            repository.save(reservation);

            entityManager.getTransaction().commit();

            Long reservationId = reservation.getId();

            assertThat(reservationId).isNotNull();

            // READ
            entityManager.clear();

            Reservation savedReservation =
                    repository.findById(reservationId).orElseThrow();

            assertThat(savedReservation.getStatus())
                    .isEqualTo("PENDING");

            // UPDATE
            entityManager.getTransaction().begin();

            savedReservation.setStatus("CONFIRMED");
            savedReservation.setConfirmedAt(LocalDateTime.now());

            repository.update(savedReservation);

            entityManager.getTransaction().commit();

            entityManager.clear();

            Reservation updatedReservation =
                    repository.findById(reservationId).orElseThrow();

            assertThat(updatedReservation.getStatus())
                    .isEqualTo("CONFIRMED");

            // JOIN FETCH - N+1 SOLUTION
            entityManager.clear();

            List<Reservation> reservations =
                    repository.findAllWithDetails();

            assertThat(reservations).hasSize(1);

            Reservation reservationWithDetails =
                    reservations.get(0);

            assertThat(reservationWithDetails.getStudent().getFullName())
                    .isEqualTo("Juan Diego Bedoya");

            assertThat(
                    reservationWithDetails
                            .getTimeSlot()
                            .getStudyRoom()
                            .getName()
            ).isEqualTo("Room 202");

            // DELETE
            entityManager.getTransaction().begin();

            repository.delete(reservationWithDetails);

            entityManager.getTransaction().commit();

            entityManager.clear();

            assertThat(repository.findById(reservationId))
                    .isEmpty();

            System.out.println(
                    "Reservation CRUD and JOIN FETCH integration test passed."
            );

        } finally {

            if (entityManager.isOpen()) {
                entityManager.close();
            }

            if (entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
            }
        }
    }
}