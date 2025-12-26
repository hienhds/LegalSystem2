package com.example.backend.case_management.dto;

import com.example.backend.case_management.entity.CaseDocument;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CaseDocumentResponse {
    private Long docId;
    private String fileName;
    private String fileUrl;
    private String uploadedByName;
    private LocalDateTime uploadedAt;

    public static CaseDocumentResponse from(CaseDocument doc) {
        return CaseDocumentResponse.builder()
                .docId(doc.getDocId())
                .fileName(doc.getFileName())
                .fileUrl(doc.getFileUrl())
                .uploadedByName(doc.getUploadedBy().getFullName())
                .uploadedAt(doc.getUploadedAt())
                .build();
    }
}