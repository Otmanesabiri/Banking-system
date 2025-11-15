package com.bank.gatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        log.info("Incoming request: {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getURI());
        return chain.filter(exchange)
                .doOnSuccess(unused -> logCompletion(exchange, start, null))
                .doOnError(throwable -> logCompletion(exchange, start, throwable));
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private void logCompletion(ServerWebExchange exchange, long startTime, Throwable throwable) {
        long duration = System.currentTimeMillis() - startTime;
        if (throwable == null) {
            log.info("Completed request: {} {} in {} ms", exchange.getRequest().getMethod(), exchange.getRequest().getURI(), duration);
        } else {
            log.error("Request failed: {} {} in {} ms - {}", exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI(), duration, throwable.getMessage(), throwable);
        }
    }
}
