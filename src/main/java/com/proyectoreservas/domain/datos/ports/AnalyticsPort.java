package com.proyectoreservas.domain.datos.ports;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
public interface AnalyticsPort { List<Map<String,Object>> summarize(JsonNode rows); }
