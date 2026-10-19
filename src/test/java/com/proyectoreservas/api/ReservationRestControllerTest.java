
package com.proyectoreservas.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReservationRestControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        ReservationRestController controller =
                new ReservationRestController();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void shouldRejectUnauthenticatedReservationList()
            throws Exception {

        mockMvc.perform(get("/api/v1/reservas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error")
                        .value("Debes iniciar sesión."));
    }

    @Test
    void shouldRejectUnauthenticatedReservationCreation()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"timeSlotId\":62}")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnauthenticatedCancellation()
            throws Exception {

        mockMvc.perform(delete("/api/v1/reservas/12"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectNegativeTimeSlotId()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/reservas")
                        .session(authenticatedSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"timeSlotId\":-1}")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMissingTimeSlotId()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/reservas")
                        .session(authenticatedSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectNegativeReservationId()
            throws Exception {

        mockMvc.perform(
                delete("/api/v1/reservas/-1")
                        .session(authenticatedSession())
        ).andExpect(status().isBadRequest());
    }

    private MockHttpSession authenticatedSession() {

        MockHttpSession session = new MockHttpSession();

        session.setAttribute("studentId", 1L);
        session.setAttribute("studentName", "Estudiante Prueba");
        session.setAttribute("institutionalCode", "EST001");

        return session;
    }
}
