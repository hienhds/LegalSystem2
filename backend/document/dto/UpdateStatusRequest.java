package com.example.backend.document.dto;

import com.example.backend.document.entity.LegalDocument.DocumentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {
    @NotNull(message = "Trạng thái không được để trống")
    private DocumentStatus status;
}
