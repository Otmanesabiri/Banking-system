package com.bank.virementservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI virementOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Virement Service API")
                        .description("API documentation for Virement Service")
                        .version("1.0.0"));
    }
}
