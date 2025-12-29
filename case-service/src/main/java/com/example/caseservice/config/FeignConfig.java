package com.example.caseservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                // Lấy token từ header Authorization của request hiện tại
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null) {
                    // Đính kèm token này vào request gửi sang User-Service
                    requestTemplate.header("Authorization", authHeader);
                }
            }
        };
    }
}