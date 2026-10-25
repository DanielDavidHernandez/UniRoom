package com.proyectoreservas.domain.datos;
import java.util.List;
public final class DatasetCatalog {
    private DatasetCatalog() {}
    public static final Dataset INSTITUCIONES = new Dataset("n5yy-8nav", "Instituciones de educación superior", "MEN - SNIES");
    public static final Dataset MATRICULA = new Dataset("y9ga-zwzy", "Matrícula por municipios", "MEN - Estadísticas de matrícula");
    public static List<Dataset> all() { return List.of(INSTITUCIONES, MATRICULA); }
    public static Dataset require(String id) { return all().stream().filter(d -> d.id().equals(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("Dataset no permitido")); }
}
