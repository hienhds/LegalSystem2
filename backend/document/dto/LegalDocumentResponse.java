package com.example.backend.document.dto;

import com.example.backend.document.entity.LegalDocument.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegalDocumentResponse {
    
    private Long documentId;
    private String title;
    private String category;
    private String fileUrl;
    private DocumentStatus status;
    private Integer viewCount;
    private LocalDateTime createdAt;
    
    // Static factory method to convert from Entity
    public static LegalDocumentResponse fromEntity(com.example.backend.document.entity.LegalDocument document) {
        return LegalDocumentResponse.builder()
            .documentId(document.getDocumentId())
            .title(document.getTitle())
            .category(document.getCategory())
            .fileUrl(document.getFileUrl())
            .status(document.getStatus())
            .viewCount(document.getViewCount())
            .createdAt(document.getCreatedAt())
            .build();
    }
}