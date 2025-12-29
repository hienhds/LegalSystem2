package com.example.caseservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    
                    // Lấy các Header quan trọng từ Request hiện tại và gắn vào Feign Request
                    String userId = request.getHeader("X-User-Id");
                    String userRole = request.getHeader("X-User-Role");
                    String authHeader = request.getHeader("Authorization");

                    if (userId != null) template.header("X-User-Id", userId);
                    if (userRole != null) template.header("X-User-Role", userRole);
                    if (authHeader != null) template.header("Authorization", authHeader);
                }
            }
        };
    }
}