package com.example.smartcampus.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartCampusOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Smart Campus Management API")
                .version("1.0.0")
                .description("REST API for students, faculty, departments, courses, attendance and marks. "
                        + "Log in at http://localhost:8080 first; Swagger uses the same session cookie."));
    }
}
