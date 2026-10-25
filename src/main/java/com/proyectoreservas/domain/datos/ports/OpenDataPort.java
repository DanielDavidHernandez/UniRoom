package com.proyectoreservas.domain.datos.ports;
import com.fasterxml.jackson.databind.JsonNode;
public interface OpenDataPort { JsonNode fetch(String datasetId, int limit) throws Exception; }
