
package com.proyectoreservas.api;

import config.JpaUtil;
import entity.StudyRoom;
import entity.TimeSlot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(
        name = "Study Rooms",
        description = "Operations for browsing university study rooms and their schedules."
)
public class RoomRestController {

    private final EntityManagerFactory entityManagerFactory;

    public RoomRestController() {
        this.entityManagerFactory = JpaUtil.getEntityManagerFactory();
    }

    // CONSULTAR TODAS LAS SALAS ACTIVAS
    @Operation(
            summary = "List active study rooms",
            description = "Retrieves all active study rooms, ordered by their identifiers. "
                    + "Each room includes its identifier, name, location, and capacity.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Study rooms retrieved successfully.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Study rooms",
                                            value = """
                                                    [
                                                      {
                                                        "id": 2,
                                                        "name": "Sombrillas",
                                                        "location": "Zona de sombrillas",
                                                        "capacity": 6
                                                      },
                                                      {
                                                        "id": 3,
                                                        "name": "Biblioteca",
                                                        "location": "Biblioteca",
                                                        "capacity": 20
                                                      }
                                                    ]
                                                    """
                                    )
                            )
                    )
            }
    )
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
    @Operation(
            summary = "Get a study room by ID",
            description = "Retrieves the details of a specific study room "
                    + "using its unique identifier.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Study room found.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Study room",
                                            value = """
                                                    {
                                                      "id": 2,
                                                      "name": "Sombrillas",
                                                      "location": "Zona de sombrillas",
                                                      "capacity": 6
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Study room not found.",
                            content = @Content
                    )
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoom(
            @Parameter(
                    description = "Unique identifier of the study room.",
                    required = true,
                    example = "2"
            )
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
    @Operation(
            summary = "List schedules for a study room",
            description = "Retrieves all time slots associated with a study room, "
                    + "ordered by their start date and time. "
                    + "Each time slot includes its identifier, date, "
                    + "start time, end time, and availability status.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Study room schedules retrieved successfully.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Room schedules",
                                            value = """
                                                    [
                                                      {
                                                        "id": 62,
                                                        "fecha": "2026-10-19",
                                                        "horaInicio": "08:00:00",
                                                        "horaFin": "10:00:00",
                                                        "estado": "AVAILABLE"
                                                      },
                                                      {
                                                        "id": 132,
                                                        "fecha": "2026-10-19",
                                                        "horaInicio": "10:00:00",
                                                        "horaFin": "12:00:00",
                                                        "estado": "AVAILABLE"
                                                      }
                                                    ]
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Study room not found.",
                            content = @Content
                    )
            }
    )
    @GetMapping("/{id}/horarios")
    public ResponseEntity<List<TimeSlotResponse>> getRoomSchedules(
            @Parameter(
                    description = "Identifier of the study room whose schedules will be retrieved.",
                    required = true,
                    example = "2"
            )
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
    @Schema(description = "Public information about a university study room.")
    public record RoomResponse(

            @Schema(
                    description = "Unique room identifier.",
                    example = "2"
            )
            Long id,

            @Schema(
                    description = "Name of the study room.",
                    example = "Sombrillas"
            )
            String name,

            @Schema(
                    description = "Physical location of the study room.",
                    example = "Zona de sombrillas"
            )
            String location,

            @Schema(
                    description = "Maximum room capacity.",
                    example = "6"
            )
            Integer capacity
    ) {}

    // DTO HORARIO
    @Schema(description = "Scheduled time slot for a study room.")
    public record TimeSlotResponse(

            @Schema(
                    description = "Unique time slot identifier.",
                    example = "62"
            )
            Long id,

            @Schema(
                    description = "Calendar date of the time slot.",
                    example = "2026-10-19",
                    type = "string",
                    format = "date"
            )
            LocalDate fecha,

            @Schema(
                    description = "Starting time of the time slot.",
                    example = "08:00:00",
                    type = "string"
            )
            LocalTime horaInicio,

            @Schema(
                    description = "Ending time of the time slot.",
                    example = "10:00:00",
                    type = "string"
            )
            LocalTime horaFin,

            @Schema(
                    description = "Availability status of the time slot.",
                    example = "AVAILABLE"
            )
            String estado
    ) {}
}
