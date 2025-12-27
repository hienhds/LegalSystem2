package com.example.documentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChuongResponse {

    private String id;
    private String deMucId;
    private String text;
    private String chuDeId;
}
