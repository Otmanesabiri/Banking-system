package com.bank.virementservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.bank.virementservice.client")
public class VirementServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VirementServiceApplication.class, args);
    }
}
