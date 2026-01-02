package com.example.apigateway.config;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter implements GlobalFilter {

    private final JwtUtils jwtUtils;

    private static final List<String> EXCLUDE_URLS = List.of("/api/auth/login", "/api/auth/register", "/api/auth/refresh");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        if (EXCLUDE_URLS.stream().anyMatch(path::contains)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            if (jwtUtils.validateToken(token)) {
                Claims claims = jwtUtils.getClaims(token);
                Object uid = claims.get("uid");
                Object role = claims.get("role"); // <--- LẤY ROLE TỪ CLAIM
                
                if (uid != null) {
                    // Chèn cả Id và Role vào header để Case Service nhận được
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-Id", String.valueOf(uid))
                            .header("X-User-Role", role != null ? String.valueOf(role) : "") // <--- GỬI HEADER ROLE
                            .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                }
            }
        } catch (Exception e) {
            log.error("Gateway: JWT validation failed: {}", e.getMessage());
        }

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}