package com.apitestinghub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI Configuration for API Testing Hub
 *
 * Configures Swagger/OpenAPI documentation for the REST API.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/api/v1}")
    private String contextPath;

    @Bean
    public OpenAPI apiTestingHubOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Testing Hub")
                        .description("A Postman-like API testing tool with documentation generation capabilities")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("API Testing Hub Team")
                                .email("support@api-testing-hub.com")
                                .url("https://api-testing-hub.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080" + contextPath)
                                .description("Development Server"),
                        new Server()
                                .url("https://api.api-testing-hub.com" + contextPath)
                                .description("Production Server")
                ));
    }
}