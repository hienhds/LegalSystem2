package com.example.userservice.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationChartResponse {
    private List<String> labels;
    private List<Long> lawyers;
    private List<Long> users;
}
