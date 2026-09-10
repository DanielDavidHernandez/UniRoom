package com.proyectoreservas.web;

import config.JpaUtil;
import entity.StudyRoom;
import entity.TimeSlot;
import entity.WaitlistEntry;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import repository.StudyRoomRepository;
import repository.TimeSlotRepository;
import repository.WaitlistRepository;
import service.ReservationService;
import service.WaitlistService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class RoomController {

    @GetMapping("/salas/{id}")
    public String roomDetail(
            @PathVariable("id") Long id,
            Model model,
            HttpSession session) {

        EntityManager entityManager =
                JpaUtil.getEntityManagerFactory().createEntityManager();

        try {

            StudyRoomRepository roomRepository =
                    new StudyRoomRepository(entityManager);

            TimeSlotRepository timeSlotRepository =
                    new TimeSlotRepository(entityManager);

            StudyRoom room =
                    roomRepository
                            .findById(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Sala no encontrada."
                                    )
                            );

            List<TimeSlot> timeSlots =
                    timeSlotRepository
                            .findByRoomId(id);

            Long studentId =
                    (Long) session.getAttribute(
                            "studentId"
                    );

            String studentName =
                    (String) session.getAttribute(
                            "studentName"
                    );

            Map<Long, Integer> waitlistPositions =
                    new HashMap<>();

            if (studentId != null) {

                WaitlistRepository waitlistRepository =
                        new WaitlistRepository(entityManager);

                List<WaitlistEntry> entries =
                        waitlistRepository
                                .findWaitingByStudentIdWithDetails(
                                        studentId
                                );

                for (WaitlistEntry entry : entries) {

                    waitlistPositions.put(
                            entry.getTimeSlot().getId(),
                            entry.getPosition()
                    );
                }
            }

            model.addAttribute(
                    "room",
                    room
            );

            model.addAttribute(
                    "timeSlots",
                    timeSlots
            );

            model.addAttribute(
                    "loggedIn",
                    studentId != null
            );

            model.addAttribute(
                    "studentName",
                    studentName
            );

            model.addAttribute(
                    "waitlistPositions",
                    waitlistPositions
            );

            return "room-detail";

        } finally {
            entityManager.close();
        }
    }

    @PostMapping("/salas/{roomId}/reservar")
    public String reserveRoom(
            @PathVariable("roomId") Long roomId,
            @RequestParam("timeSlotId") Long timeSlotId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long studentId =
                (Long) session.getAttribute(
                        "studentId"
                );

        String studentName =
                (String) session.getAttribute(
                        "studentName"
                );

        if (studentId == null) {
            return "redirect:/login";
        }

        try {

            ReservationService reservationService =
                    new ReservationService();

            Long reservationId =
                    reservationService
                            .confirmReservation(
                                    studentId,
                                    timeSlotId
                            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Reserva confirmada para "
                            + studentName
                            + ". Reserva N.º "
                            + reservationId
            );

        } catch (RuntimeException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/salas/" + roomId;
    }

    @PostMapping("/salas/{roomId}/lista-espera")
    public String joinWaitlist(
            @PathVariable("roomId") Long roomId,
            @RequestParam("timeSlotId") Long timeSlotId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long studentId =
                (Long) session.getAttribute(
                        "studentId"
                );

        if (studentId == null) {
            return "redirect:/login";
        }

        try {

            WaitlistService waitlistService =
                    new WaitlistService();

            int position =
                    waitlistService.joinWaitlist(
                            studentId,
                            timeSlotId
                    );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Te uniste a la lista de espera. " +
                            "Tu posición es: " + position
            );

        } catch (RuntimeException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/salas/" + roomId;
    }
}