package com.hrapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI at /swagger-ui.html
 * The "Authorize" button accepts a JWT (paste the token from /api/auth/login, without "Bearer ").
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "Bearer JWT";

    @Bean
    public OpenAPI hrAppOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartHR API")
                        .description("AI-powered HR Management System")
                        .version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME))
                .components(new Components().addSecuritySchemes(SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
