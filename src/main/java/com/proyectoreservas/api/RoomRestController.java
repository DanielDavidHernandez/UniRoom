
package com.proyectoreservas.api;

import config.JpaUtil;
import entity.StudyRoom;
import entity.TimeSlot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/salas")
public class RoomRestController {

    private final EntityManagerFactory entityManagerFactory;

    public RoomRestController() {
        this.entityManagerFactory = JpaUtil.getEntityManagerFactory();
    }

    // CONSULTAR TODAS LAS SALAS ACTIVAS
    @GetMapping
    public List<RoomResponse> listRooms() {

        EntityManager entityManager =
                entityManagerFactory.createEntityManager();

        try {

            List<StudyRoom> rooms = entityManager.createQuery(
                    "SELECT r FROM StudyRoom r " +
                    "WHERE r.isActive = true " +
                    "ORDER BY r.id",
                    StudyRoom.class
            ).getResultList();

            return rooms.stream()
                    .map(this::toResponse)
                    .toList();

        } finally {
            entityManager.close();
        }
    }

    // CONSULTAR UNA SALA POR ID
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoom(
            @PathVariable("id") Long id) {

        EntityManager entityManager =
                entityManagerFactory.createEntityManager();

        try {

            StudyRoom room = entityManager.find(
                    StudyRoom.class,
                    id
            );

            if (room == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(toResponse(room));

        } finally {
            entityManager.close();
        }
    }

    // CONSULTAR HORARIOS DE UNA SALA
    @GetMapping("/{id}/horarios")
    public ResponseEntity<List<TimeSlotResponse>> getRoomSchedules(
            @PathVariable("id") Long id) {

        EntityManager entityManager =
                entityManagerFactory.createEntityManager();

        try {

            StudyRoom room = entityManager.find(
                    StudyRoom.class,
                    id
            );

            if (room == null) {
                return ResponseEntity.notFound().build();
            }

            List<TimeSlot> timeSlots = entityManager.createQuery(
                    "SELECT t FROM TimeSlot t " +
                    "WHERE t.studyRoom.id = :roomId " +
                    "ORDER BY t.startTime",
                    TimeSlot.class
            )
            .setParameter("roomId", id)
            .getResultList();

            List<TimeSlotResponse> response = timeSlots.stream()
                    .map(this::toTimeSlotResponse)
                    .toList();

            return ResponseEntity.ok(response);

        } finally {
            entityManager.close();
        }
    }

    // CONVERTIR ENTIDAD SALA A RESPUESTA JSON
    private RoomResponse toResponse(StudyRoom room) {

        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getLocation(),
                room.getCapacity()
        );
    }

    // CONVERTIR ENTIDAD HORARIO A RESPUESTA JSON
    private TimeSlotResponse toTimeSlotResponse(TimeSlot timeSlot) {

        return new TimeSlotResponse(
                timeSlot.getId(),
                timeSlot.getStartTime().toLocalDate(),
                timeSlot.getStartTime().toLocalTime(),
                timeSlot.getEndTime().toLocalTime(),
                timeSlot.getStatus()
        );
    }

    // DTO SALA
    public record RoomResponse(
            Long id,
            String name,
            String location,
            Integer capacity
    ) {}

    // DTO HORARIO
    public record TimeSlotResponse(
            Long id,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            String estado
    ) {}
}
