package com.bank.chatbotservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration
 */
@Configuration
public class OpenAPIConfig {
    
    @Bean
    public OpenAPI chatbotServiceAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8083");
        devServer.setDescription("Server URL in Development environment");
        
        Server prodServer = new Server();
        prodServer.setUrl("http://localhost:8080/chatbot-service");
        prodServer.setDescription("Server URL via Gateway in Production environment");
        
        Contact contact = new Contact();
        contact.setEmail("support@bank.com");
        contact.setName("Banking System Support");
        
        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");
        
        Info info = new Info()
                .title("Chatbot Service API")
                .version("1.0.0")
                .contact(contact)
                .description("API du service de chatbot intelligent avec RAG (Retrieval Augmented Generation) pour le système bancaire. " +
                        "Ce service utilise Spring AI et GPT-4o pour fournir une assistance intelligente aux utilisateurs.")
                .termsOfService("https://www.bank.com/terms")
                .license(mitLicense);
        
        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}
