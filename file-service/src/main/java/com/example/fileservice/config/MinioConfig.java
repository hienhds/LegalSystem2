package com.example.fileservice.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MinioConfig {

    @Bean
    public MinioClient internalMinioClient(
            @Value("${minio.url}") String endpoint,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey
    ) {
        return MinioClient.builder()
//                .endpoint(endpoint)
                .endpoint("http://localhost", 9000, false)
                .credentials(accessKey, secretKey)
                .build();
    }



}
