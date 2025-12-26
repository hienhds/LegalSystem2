package com.example.backend.document.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkDeleteRequest {
    @NotEmpty(message = "Danh sách ID không được rỗng")
    private List<Long> ids;
}
