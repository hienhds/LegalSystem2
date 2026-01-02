package com.example.userservice.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${upload.documents.path:uploads/documents}")
    private String documentUploadPath;
    
    @Value("${upload.avatar.path:uploads/avatar}")
    private String avatarUploadPath;
    
    @Value("${upload.certificate.path:uploads/certificates}")
    private String certificateUploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Document uploads
        Path documentPath = Paths.get(documentUploadPath).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/documents/**")
                .addResourceLocations("file:" + documentPath.toString() + "/");
        
        // Avatar uploads
        Path avatarPath = Paths.get(avatarUploadPath).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/avatar/**")
                .addResourceLocations("file:" + avatarPath.toString() + "/");
        
        // Certificate uploads
        Path certificatePath = Paths.get(certificateUploadPath).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/certificates/**")
                .addResourceLocations("file:" + certificatePath.toString() + "/");
    }
}
