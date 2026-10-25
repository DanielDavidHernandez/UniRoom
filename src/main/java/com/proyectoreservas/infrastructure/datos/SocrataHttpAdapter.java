package com.proyectoreservas.infrastructure.datos;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoreservas.domain.datos.ports.OpenDataPort;
import org.springframework.web.client.RestClient;
import java.time.Duration;
public class SocrataHttpAdapter implements OpenDataPort {
    private final RestClient rest;
    private final ObjectMapper json;
    public SocrataHttpAdapter(RestClient rest, ObjectMapper json) { this.rest=rest; this.json=json; }
    @Override public JsonNode fetch(String datasetId, int limit) throws Exception {
        Exception last=null;
        for (int attempt=0; attempt<3; attempt++) {
            try {
                String body=rest.get().uri("/resource/{id}.json?$limit={limit}",datasetId,limit).retrieve().body(String.class);
                JsonNode result=json.readTree(body);
                if (!result.isArray()) throw new IllegalStateException("La API no devolvió un arreglo JSON");
                return result;
            } catch (Exception e) {
                last=e;
                if (attempt<2) Thread.sleep(Duration.ofMillis(250L * (1L << attempt)).toMillis());
            }
        }
        throw last;
    }
}
