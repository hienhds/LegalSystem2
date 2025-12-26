package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Allow credentials
        config.setAllowCredentials(true);
        
        // Allow all origins for development
        config.addAllowedOriginPattern("*");
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:3000");
        
        // Allow all headers
        config.addAllowedHeader("*");
        
        // Allow all methods
        config.addAllowedMethod("*");
        
        // Apply CORS configuration to all paths including WebSocket
        source.registerCorsConfiguration("/**", config);
        source.registerCorsConfiguration("/chat/**", config);
        
        return new CorsFilter(source);
    }
}
