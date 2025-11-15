package com.bank.gatewayservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final DiscoveryClient discoveryClient;

    @Bean
    public RouteLocator fallbackStaticRoutes(RouteLocatorBuilder builder) {
        if (!CollectionUtils.isEmpty(discoveryClient.getServices())) {
            return builder.routes().build();
        }

        return builder.routes()
                .route("beneficiaire-service", r -> r.path("/api/beneficiaires/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8081"))
                .route("virement-service", r -> r.path("/api/virements/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8082"))
                .route("chatbot-service", r -> r.path("/api/chatbot/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8083"))
                .build();
    }
}
