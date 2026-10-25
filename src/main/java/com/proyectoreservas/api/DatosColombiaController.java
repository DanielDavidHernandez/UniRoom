package com.proyectoreservas.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.proyectoreservas.application.datos.*;
import com.proyectoreservas.domain.datos.DatasetCatalog;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/datos-colombia")
public class DatosColombiaController {

    private final OpenDataService openData;
    private final AnalyticsService analytics;

    public DatosColombiaController(
            OpenDataService openData,
            AnalyticsService analytics) {
        this.openData = openData;
        this.analytics = analytics;
    }

    @GetMapping("/fuentes")
    public Object fuentes() {
        return DatasetCatalog.all();
    }

    @GetMapping("/{dataset}/estadisticas")
    public ResponseEntity<?> stats(
            @PathVariable("dataset") String dataset,
            @RequestParam(name = "limit", defaultValue = "100") int limit) {

        try {
            JsonNode data = openData.fetch(dataset, limit);

            return ResponseEntity.ok(Map.of(
                    "fuente", DatasetCatalog.require(dataset),
                    "registros", data.size(),
                    "circuitBreaker", openData.state(dataset).toString(),
                    "estadisticas", analytics.summarize(data)
            ));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "error", "Fuente externa temporalmente no disponible",
                            "circuitBreaker", openData.state(dataset).toString()
                    ));
        }
    }
}