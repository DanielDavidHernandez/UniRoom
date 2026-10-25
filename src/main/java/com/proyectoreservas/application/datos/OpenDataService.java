package com.proyectoreservas.application.datos;
import com.fasterxml.jackson.databind.JsonNode;
import com.proyectoreservas.domain.datos.CircuitBreaker;
import com.proyectoreservas.domain.datos.DatasetCatalog;
import com.proyectoreservas.domain.datos.ports.OpenDataPort;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class OpenDataService {
    private final OpenDataPort client;
    private final Map<String,CircuitBreaker> breakers = new ConcurrentHashMap<>();
    public OpenDataService(OpenDataPort client) { this.client=client; }
    public JsonNode fetch(String dataset, int limit) throws Exception {
        DatasetCatalog.require(dataset);
        if (limit < 1 || limit > 500) throw new IllegalArgumentException("limit debe estar entre 1 y 500");
        return breakers.computeIfAbsent(dataset, id -> new CircuitBreaker(3, java.time.Duration.ofSeconds(30), java.time.Clock.systemUTC()))
            .execute(() -> client.fetch(dataset,limit));
    }
    public CircuitBreaker.State state(String dataset) { DatasetCatalog.require(dataset); return breakers.computeIfAbsent(dataset, id -> new CircuitBreaker(3, java.time.Duration.ofSeconds(30), java.time.Clock.systemUTC())).state(); }
}
