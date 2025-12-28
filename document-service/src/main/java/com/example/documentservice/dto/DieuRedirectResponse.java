package com.example.documentservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DieuRedirectResponse {
    private String chuDeId;
    private String deMucId;
    private String chuongId;
    private String dieuId;
}
