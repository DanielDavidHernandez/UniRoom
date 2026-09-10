package com.proyectoreservas.web;

import config.JpaUtil;
import entity.StudyRoom;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import repository.StudyRoomRepository;

import java.util.List;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(
            Model model,
            HttpSession session) {

        EntityManager entityManager =
                JpaUtil.getEntityManagerFactory().createEntityManager();

        try {

            StudyRoomRepository repository =
                    new StudyRoomRepository(entityManager);

            List<StudyRoom> rooms =
                    repository.findActiveRooms();

            model.addAttribute(
                    "rooms",
                    rooms
            );

            Long studentId =
                    (Long) session.getAttribute("studentId");

            String studentName =
                    (String) session.getAttribute("studentName");

            model.addAttribute(
                    "loggedIn",
                    studentId != null
            );

            model.addAttribute(
                    "studentName",
                    studentName
            );

            return "index";

        } finally {
            entityManager.close();
        }
    }
}