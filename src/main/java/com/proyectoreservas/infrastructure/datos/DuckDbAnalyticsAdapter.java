package com.proyectoreservas.infrastructure.datos;
import com.fasterxml.jackson.databind.JsonNode;
import com.proyectoreservas.domain.datos.ports.AnalyticsPort;
import java.sql.*;
import java.util.*;
public class DuckDbAnalyticsAdapter implements AnalyticsPort {
    @Override public List<Map<String,Object>> summarize(JsonNode rows) {
        if (!rows.isArray()) throw new IllegalArgumentException("Se requiere JSON tipo array");
        try (Connection db=DriverManager.getConnection("jdbc:duckdb:"); Statement st=db.createStatement()) {
            st.execute("CREATE TABLE datos (categoria VARCHAR, atributos INTEGER)");
            try (PreparedStatement insert=db.prepareStatement("INSERT INTO datos VALUES (?, ?)") ) {
                for (JsonNode row: rows) {
                    String category= firstText(row,"nombre_del_departamento","nombre_instituci_n","nombre_del_municipio");
                    int fields=row.isObject()?row.size():0;
                    insert.setString(1,category); insert.setInt(2,fields); insert.addBatch();
                }
                insert.executeBatch();
            }
            List<Map<String,Object>> result=new ArrayList<>();
            // Query 1: basic descriptive statistics of record completeness
            try (ResultSet rs=st.executeQuery("SELECT COUNT(*) cantidad, ROUND(AVG(atributos),2) promedio_campos, MIN(atributos) minimo_campos, MAX(atributos) maximo_campos FROM datos")) {
                while (rs.next()) result.add(Map.of("consulta","estadisticas", "cantidad",rs.getLong(1),"promedioCampos",rs.getDouble(2),"minimoCampos",rs.getInt(3),"maximoCampos",rs.getInt(4)));
            }
            // Query 2: frequency by a dataset-specific geographic/institution label
            try (ResultSet rs=st.executeQuery("SELECT categoria, COUNT(*) cantidad FROM datos GROUP BY categoria ORDER BY cantidad DESC, categoria LIMIT 10")) {
                while (rs.next()) result.add(Map.of("consulta","frecuencias", "categoria",rs.getString(1),"cantidad",rs.getLong(2)));
            }
            return result;
        } catch (SQLException e) { throw new IllegalStateException("Falló el análisis DuckDB", e); }
    }
    private String firstText(JsonNode row,String... keys) {
        for (String k:keys) if (row.hasNonNull(k) && !row.path(k).asText().isBlank()) return row.path(k).asText();
        return "SIN CLASIFICAR";
    }
}
