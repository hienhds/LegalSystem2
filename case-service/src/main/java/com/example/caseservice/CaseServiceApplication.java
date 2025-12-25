package com.example.caseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients // Annotation này cực kỳ quan trọng để gọi sang User-Service sau này
public class CaseServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CaseServiceApplication.class, args);
    }
}