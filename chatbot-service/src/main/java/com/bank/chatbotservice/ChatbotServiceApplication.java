package com.bank.chatbotservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {
    org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration.class
})
@EnableDiscoveryClient
@EnableFeignClients
public class ChatbotServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatbotServiceApplication.class, args);
    }
}
