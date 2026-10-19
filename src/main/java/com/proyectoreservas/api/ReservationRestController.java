
package com.proyectoreservas.api;

import config.JpaUtil;
import entity.Reservation;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

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
public class ReservationRestController {

    // CREAR UNA RESERVA
    @PostMapping
    public ResponseEntity<?> createReservation(
            @RequestBody(required = false) CreateReservationRequest request,
            HttpServletRequest httpRequest) {

        Long studentId = getAuthenticatedStudentId(httpRequest);

        if (studentId == null) {
            return error(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión.");
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
    @GetMapping
    public ResponseEntity<?> getMyReservations(
            HttpServletRequest httpRequest) {

        Long studentId = getAuthenticatedStudentId(httpRequest);

        if (studentId == null) {
            return error(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión.");
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
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReservation(
            @PathVariable("id") Long reservationId,
            HttpServletRequest httpRequest) {

        Long studentId = getAuthenticatedStudentId(httpRequest);

        if (studentId == null) {
            return error(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión.");
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
                    "message", "Reserva cancelada correctamente."
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

    // OBTENER IDENTIDAD DESDE LA SESIÓN
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

    // RESPUESTAS DE ERROR JSON
    private ResponseEntity<Map<String, String>> error(
            HttpStatus status,
            String message) {

        return ResponseEntity
                .status(status)
                .body(Map.of("error", message));
    }

    // DTO PARA CREAR RESERVAS
    public record CreateReservationRequest(
            Long timeSlotId
    ) {}

    // DTO PARA RESERVA CREADA
    public record ReservationCreatedResponse(
            Long id,
            String status,
            String message
    ) {}

    // DTO PARA CONSULTAR RESERVAS
    public record ReservationResponse(
            Long id,
            Long timeSlotId,
            String sala,
            LocalDateTime inicio,
            LocalDateTime fin,
            String estado
    ) {}
}
