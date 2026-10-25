package com.proyectoreservas.application.datos;
import com.proyectoreservas.domain.datos.ports.AnalyticsPort;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
public class AnalyticsService {
    private final AnalyticsPort analytics;
    public AnalyticsService(AnalyticsPort analytics) { this.analytics=analytics; }
    public List<Map<String,Object>> summarize(JsonNode data) { return analytics.summarize(data); }
}
