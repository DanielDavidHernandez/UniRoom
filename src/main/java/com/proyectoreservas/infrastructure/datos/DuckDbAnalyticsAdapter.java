package com.proyectoreservas.infrastructure.datos;

import com.fasterxml.jackson.databind.JsonNode;
import com.proyectoreservas.domain.datos.ports.AnalyticsPort;
import java.sql.*;
import java.util.*;

public class DuckDbAnalyticsAdapter implements AnalyticsPort {
    private static final String[] LEVELS = {
        "tecnica_profesional", "tecnologica", "universitaria",
        "especializacion", "maestria", "doctorado"
    };

    @Override
    public List<Map<String, Object>> summarize(JsonNode rows) {
        if (!rows.isArray()) throw new IllegalArgumentException("Se requiere JSON tipo array");
        boolean enrollment = rows.size() > 0 && rows.get(0).has("nombre_del_municipio")
                && rows.get(0).has("universitaria");
        try (Connection db = DriverManager.getConnection("jdbc:duckdb:");
             Statement statement = db.createStatement()) {
            statement.execute("CREATE TABLE datos (categoria VARCHAR, atributos INTEGER, matricula BIGINT)");
            try (PreparedStatement insert = db.prepareStatement("INSERT INTO datos VALUES (?, ?, ?)")) {
                for (JsonNode row : rows) {
                    String category;
                    long students = 0;
                    if (enrollment) {
                        String municipality = text(row, "nombre_del_municipio");
                        String year = text(row, "a_o");
                        category = municipality + (year.equals("SIN CLASIFICAR") ? "" : " (" + year + ")");
                        for (String field : LEVELS) students += number(row.path(field));
                    } else {
                        category = firstText(row, "nombre_instituci_n", "nombre_institucion", "nombre_del_departamento");
                    }
                    insert.setString(1, category);
                    insert.setInt(2, row.isObject() ? row.size() : 0);
                    insert.setLong(3, students);
                    insert.addBatch();
                }
                insert.executeBatch();
            }
            List<Map<String, Object>> result = new ArrayList<>();
            // SQL 1: descriptive statistics for the sample
            try (ResultSet rs = statement.executeQuery(
                "SELECT COUNT(*) cantidad, ROUND(AVG(atributos), 2) promedio_campos, " +
                "MIN(atributos) minimo_campos, MAX(atributos) maximo_campos, " +
                "SUM(matricula) total_matricula FROM datos")) {
                if (rs.next()) {
                    result.add(Map.of("consulta", "estadisticas", "cantidad", rs.getLong("cantidad"),
                        "promedioCampos", rs.getDouble("promedio_campos"),
                        "minimoCampos", rs.getInt("minimo_campos"),
                        "maximoCampos", rs.getInt("maximo_campos"),
                        "totalMatricula", rs.getLong("total_matricula")));
                }
            }
            // SQL 2: real enrollment by municipality and year, or frequency by institution
            String sql = enrollment
                ? "SELECT categoria, SUM(matricula) cantidad FROM datos GROUP BY categoria ORDER BY cantidad DESC, categoria LIMIT 10"
                : "SELECT categoria, COUNT(*) cantidad FROM datos GROUP BY categoria ORDER BY cantidad DESC, categoria LIMIT 10";
            try (ResultSet rs = statement.executeQuery(sql)) {
                while (rs.next()) result.add(Map.of("consulta", enrollment ? "matricula" : "frecuencias",
                    "categoria", rs.getString("categoria"), "cantidad", rs.getLong("cantidad")));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Falló el análisis DuckDB", e);
        }
    }

    private long number(JsonNode node) {
        if (node == null || node.isNull()) return 0L;
        String value = node.asText().trim();
        if (value.isBlank()) return 0L;
        try { return Long.parseLong(value); }
        catch (NumberFormatException e) { return 0L; }
    }

    private String text(JsonNode row, String key) {
        return row.hasNonNull(key) && !row.path(key).asText().isBlank()
            ? row.path(key).asText().trim() : "SIN CLASIFICAR";
    }

    private String firstText(JsonNode row, String... keys) {
        for (String key : keys) {
            String value = text(row, key);
            if (!value.equals("SIN CLASIFICAR")) return value;
        }
        return "SIN CLASIFICAR";
    }
}
