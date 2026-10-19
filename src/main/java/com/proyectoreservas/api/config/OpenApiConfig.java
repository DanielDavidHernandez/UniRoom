
package com.proyectoreservas.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI uniRoomOpenAPI() {

        Info apiInfo = new Info()
                .title("UniRoom REST API")
                .version("1.0.0")
                .description(
                        "REST API for UniRoom, a university " +
                        "study room reservation management system. " +
                        "This API allows students to browse available " +
                        "study rooms, check schedules, create reservations, " +
                        "view reservation history, and cancel reservations."
                )
                .contact(
                        new Contact()
                                .name("UniRoom Development Team")
                );

        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local development server");

        return new OpenAPI()
                .info(apiInfo)
                .servers(List.of(localServer));
    }
}
