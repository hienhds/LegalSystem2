package com.example.backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${upload.documents.path:uploads/documents}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path documentUploadPath = Paths.get(uploadPath).toAbsolutePath().normalize();
        
        registry.addResourceHandler("/uploads/documents/**")
                .addResourceLocations("file:" + documentUploadPath.toString() + "/");
    }
}
