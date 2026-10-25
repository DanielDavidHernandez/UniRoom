package com.proyectoreservas.infrastructure.datos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoreservas.application.datos.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class DatosConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public OpenDataService openDataService(ObjectMapper mapper) {

        var requestFactory = new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(4000);
        requestFactory.setReadTimeout(6000);

        RestClient client = RestClient.builder()
                .baseUrl("https://www.datos.gov.co")
                .requestFactory(requestFactory)
                .build();

        return new OpenDataService(
                new SocrataHttpAdapter(client, mapper)
        );
    }

    @Bean
    public AnalyticsService analyticsService() {
        return new AnalyticsService(
                new DuckDbAnalyticsAdapter()
        );
    }
}