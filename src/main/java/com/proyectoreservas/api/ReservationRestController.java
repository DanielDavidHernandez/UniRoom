
package com.proyectoreservas.api;

import config.JpaUtil;
import entity.Reservation;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import repository.ReservationRepository;
import service.ReservationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reservas")
@Tag(
        name = "Reservations",
        description = "Operations for creating, listing, and cancelling study room reservations."
)
public class ReservationRestController {

    // CREAR UNA RESERVA
    @Operation(
            summary = "Create a study room reservation",
            description = "Creates a reservation for the authenticated student. " +
                    "The student identity is obtained from the existing HTTP session. " +
                    "The request only requires a valid time slot identifier.",
            requestBody = @RequestBody(
                    required = true,
                    description = "Identifier of the time slot to reserve.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CreateReservationRequest.class
                            ),
                            examples = @ExampleObject(
                                    name = "Reservation request",
                                    value = """
                                            {
                                              "timeSlotId": 62
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Reservation created successfully.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = ReservationCreatedResponse.class
                                    ),
                                    examples = @ExampleObject(
                                            name = "Created reservation",
                                            value = """
                                                    {
                                                      "id": 12,
                                                      "status": "CONFIRMED",
                                                      "message": "Reserva creada correctamente."
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid or missing time slot identifier."
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "The student is not authenticated."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The requested time slot does not exist."
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "The reservation cannot be created due to a conflict."
                    )
            }
    )
    @PostMapping
    public ResponseEntity<?> createReservation(
            @org.springframework.web.bind.annotation.RequestBody(required = false)
            CreateReservationRequest request,
            HttpServletRequest httpRequest) {

        Long studentId = getAuthenticatedStudentId(httpRequest);

        if (studentId == null) {
            return error(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión."
            );
        }

        if (request == null ||
                request.timeSlotId() == null ||
                request.timeSlotId() <= 0) {

            return error(
                    HttpStatus.BAD_REQUEST,
                    "Debes indicar un timeSlotId válido."
            );
        }

        try {

            ReservationService service = new ReservationService();

            Long reservationId = service.confirmReservation(
                    studentId,
                    request.timeSlotId()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ReservationCreatedResponse(
                            reservationId,
                            "CONFIRMED",
                            "Reserva creada correctamente."
                    ));

        } catch (IllegalArgumentException exception) {

            return error(
                    HttpStatus.NOT_FOUND,
                    exception.getMessage()
            );

        } catch (IllegalStateException exception) {

            return error(
                    HttpStatus.CONFLICT,
                    exception.getMessage()
            );
        }
    }

    // CONSULTAR MIS RESERVAS
    @Operation(
            summary = "List my reservations",
            description = "Returns the reservation history of the authenticated student, " +
                    "including confirmed and cancelled reservations. " +
                    "Authentication is required through the UniRoom HTTP session.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reservation history retrieved successfully.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Reservation history",
                                            value = """
                                                    [
                                                      {
                                                        "id": 12,
                                                        "timeSlotId": 62,
                                                        "sala": "Sombrillas",
                                                        "inicio": "2026-10-19T08:00:00",
                                                        "fin": "2026-10-19T10:00:00",
                                                        "estado": "CANCELLED"
                                                      }
                                                    ]
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "The student is not authenticated."
                    )
            }
    )
    @GetMapping
    public ResponseEntity<?> getMyReservations(
            HttpServletRequest httpRequest) {

        Long studentId = getAuthenticatedStudentId(httpRequest);

        if (studentId == null) {
            return error(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión."
            );
        }

        EntityManager entityManager =
                JpaUtil.getEntityManagerFactory().createEntityManager();

        try {

            ReservationRepository repository =
                    new ReservationRepository(entityManager);

            List<ReservationResponse> response =
                    repository.findByStudentIdWithDetails(studentId)
                            .stream()
                            .map(reservation -> new ReservationResponse(
                                    reservation.getId(),
                                    reservation.getTimeSlot().getId(),
                                    reservation.getTimeSlot()
                                            .getStudyRoom().getName(),
                                    reservation.getTimeSlot().getStartTime(),
                                    reservation.getTimeSlot().getEndTime(),
                                    reservation.getStatus()
                            ))
                            .toList();

            return ResponseEntity.ok(response);

        } finally {
            entityManager.close();
        }
    }

    // CANCELAR UNA RESERVA
    @Operation(
            summary = "Cancel a reservation",
            description = "Cancels an existing reservation belonging to the " +
                    "authenticated student. The operation uses the existing " +
                    "reservation service and preserves the system's waitlist logic.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reservation cancelled successfully.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Successful cancellation",
                                            value = """
                                                    {
                                                      "message": "Reserva cancelada correctamente."
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid reservation identifier."
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "The student is not authenticated."
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "The student is not allowed to cancel this reservation."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Reservation not found."
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "The reservation cannot be cancelled due to its current state."
                    )
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReservation(
            @Parameter(
                    description = "Unique identifier of the reservation to cancel.",
                    required = true,
                    example = "12"
            )
            @PathVariable("id") Long reservationId,
            HttpServletRequest httpRequest) {

        Long studentId = getAuthenticatedStudentId(httpRequest);

        if (studentId == null) {
            return error(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión."
            );
        }

        if (reservationId <= 0) {
            return error(
                    HttpStatus.BAD_REQUEST,
                    "El identificador de la reserva no es válido."
            );
        }

        try {

            ReservationService service = new ReservationService();

            service.cancelReservation(reservationId, studentId);

            return ResponseEntity.ok(Map.of(
                    "message",
                    "Reserva cancelada correctamente."
            ));

        } catch (IllegalArgumentException exception) {

            return error(
                    HttpStatus.NOT_FOUND,
                    exception.getMessage()
            );

        } catch (IllegalStateException exception) {

            if (exception.getMessage() != null &&
                    exception.getMessage().contains("otro estudiante")) {

                return error(
                        HttpStatus.FORBIDDEN,
                        "No puedes cancelar una reserva de otro estudiante."
                );
            }

            return error(
                    HttpStatus.CONFLICT,
                    exception.getMessage()
            );
        }
    }

    // OBTENER LA IDENTIDAD DEL ESTUDIANTE DESDE LA SESIÓN
    private Long getAuthenticatedStudentId(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object studentId = session.getAttribute("studentId");

        return studentId instanceof Long
                ? (Long) studentId
                : null;
    }

    // GENERAR RESPUESTAS DE ERROR EN JSON
    private ResponseEntity<Map<String, String>> error(
            HttpStatus status,
            String message) {

        return ResponseEntity
                .status(status)
                .body(Map.of("error", message));
    }

    // DTO PARA CREAR RESERVAS
    @Schema(
            description = "Request containing the identifier of the time slot to reserve."
    )
    public record CreateReservationRequest(

            @Schema(
                    description = "Unique identifier of the requested time slot.",
                    example = "62",
                    minimum = "1"
            )
            Long timeSlotId

    ) {}

    // DTO PARA RESPUESTA DE RESERVA CREADA
    @Schema(
            description = "Response returned after a successful reservation."
    )
    public record ReservationCreatedResponse(

            @Schema(
                    description = "Unique identifier of the new reservation.",
                    example = "12"
            )
            Long id,

            @Schema(
                    description = "Current reservation status.",
                    example = "CONFIRMED"
            )
            String status,

            @Schema(
                    description = "Human-readable result message.",
                    example = "Reserva creada correctamente."
            )
            String message

    ) {}

    // DTO PARA CONSULTAR RESERVAS
    @Schema(
            description = "Reservation information belonging to the authenticated student."
    )
    public record ReservationResponse(

            @Schema(
                    description = "Unique reservation identifier.",
                    example = "12"
            )
            Long id,

            @Schema(
                    description = "Identifier of the associated time slot.",
                    example = "62"
            )
            Long timeSlotId,

            @Schema(
                    description = "Name of the reserved study room.",
                    example = "Sombrillas"
            )
            String sala,

            @Schema(
                    description = "Reservation starting date and time.",
                    example = "2026-10-19T08:00:00"
            )
            LocalDateTime inicio,

            @Schema(
                    description = "Reservation ending date and time.",
                    example = "2026-10-19T10:00:00"
            )
            LocalDateTime fin,

            @Schema(
                    description = "Current reservation status.",
                    example = "CANCELLED"
            )
            String estado

    ) {}
}
