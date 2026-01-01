package com.example.caseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseDocumentResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private String filePath;
    private LocalDateTime uploadedAt;
}