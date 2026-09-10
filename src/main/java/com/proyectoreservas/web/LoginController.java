package com.proyectoreservas.web;

import config.JpaUtil;
import entity.Student;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import repository.StudentRepository;

import java.util.Optional;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam("institutionalCode") String institutionalCode,
            @RequestParam("password") String password,
            HttpSession session,
            Model model) {

        EntityManager entityManager =
                JpaUtil.getEntityManagerFactory().createEntityManager();

        try {

            StudentRepository studentRepository =
                    new StudentRepository(entityManager);

            Optional<Student> studentOptional =
                    studentRepository.findByInstitutionalCode(
                            institutionalCode.trim()
                    );

            if (studentOptional.isEmpty()) {

                model.addAttribute(
                        "errorMessage",
                        "El código institucional no está registrado."
                );

                return "login";
            }

            Student student = studentOptional.get();

            if (!student.getPasswordHash().equals(password)) {

                model.addAttribute(
                        "errorMessage",
                        "La contraseña es incorrecta."
                );

                return "login";
            }

            session.setAttribute(
                    "studentId",
                    student.getId()
            );

            session.setAttribute(
                    "studentName",
                    student.getFullName()
            );

            session.setAttribute(
                    "institutionalCode",
                    student.getInstitutionalCode()
            );

            return "redirect:/";

        } finally {
            entityManager.close();
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/";
    }
}