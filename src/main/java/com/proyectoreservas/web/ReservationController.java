package com.proyectoreservas.web;

import config.JpaUtil;
import entity.Reservation;
import entity.WaitlistEntry;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import repository.ReservationRepository;
import repository.WaitlistRepository;
import service.ReservationService;
import service.WaitlistService;

import java.util.List;

@Controller
public class ReservationController {

    @GetMapping("/mis-reservas")
    public String myReservations(
            HttpSession session,
            Model model) {

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

        EntityManager entityManager =
                JpaUtil.getEntityManagerFactory()
                        .createEntityManager();

        try {

            ReservationRepository reservationRepository =
                    new ReservationRepository(
                            entityManager
                    );

            WaitlistRepository waitlistRepository =
                    new WaitlistRepository(
                            entityManager
                    );

            List<Reservation> reservations =
                    reservationRepository
                            .findByStudentIdWithDetails(
                                    studentId
                            );

            List<WaitlistEntry> waitlistEntries =
                    waitlistRepository
                            .findWaitingByStudentIdWithDetails(
                                    studentId
                            );

            model.addAttribute(
                    "reservations",
                    reservations
            );

            model.addAttribute(
                    "waitlistEntries",
                    waitlistEntries
            );

            model.addAttribute(
                    "studentName",
                    studentName
            );

            return "my-reservations";

        } finally {
            entityManager.close();
        }
    }

    @PostMapping("/mis-reservas/{id}/cancelar")
    public String cancelReservation(
            @PathVariable("id") Long reservationId,
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

            ReservationService reservationService =
                    new ReservationService();

            reservationService.cancelReservation(
                    reservationId,
                    studentId
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "La reserva fue cancelada correctamente."
            );

        } catch (RuntimeException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/mis-reservas";
    }

    @PostMapping("/mis-reservas/lista-espera/{id}/salir")
    public String leaveWaitlist(
            @PathVariable("id") Long waitlistId,
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

            waitlistService.leaveWaitlist(
                    waitlistId,
                    studentId
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Saliste de la lista de espera."
            );

        } catch (RuntimeException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/mis-reservas";
    }
}