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
public class CaseUpdateResponse {
    private Long id;
    private String updateDescription;
    private LocalDateTime updateDate;
}