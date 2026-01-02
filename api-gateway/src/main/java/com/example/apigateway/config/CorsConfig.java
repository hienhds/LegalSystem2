package com.example.apigateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        // Cho phép Frontend truy cập
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        corsConfig.setMaxAge(3600L);
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }

    // CÁI NÀY QUAN TRỌNG: GlobalFilter này sẽ "dọn dẹp" mọi header CORS trùng lặp từ service khác gửi lên
    @Bean
    public GlobalFilter corsResponseHeaderFilter() {
        return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
            exchange.getResponse().getHeaders().entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)
                            || entry.getKey().equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS))
                    .forEach(entry -> {
                        // Nếu có nhiều hơn 1 giá trị, chỉ giữ lại giá trị đầu tiên
                        if (entry.getValue().size() > 1) {
                            String firstValue = entry.getValue().get(0);
                            exchange.getResponse().getHeaders().set(entry.getKey(), firstValue);
                        }
                    });
        }));
    }
}