package com.example.backend.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDocumentRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 500, message = "Tiêu đề không được vượt quá 500 ký tự")
    private String title;

    @NotBlank(message = "Danh mục không được để trống")
    @Size(max = 100, message = "Danh mục không được vượt quá 100 ký tự")
    private String category;

    @Size(max = 500, message = "URL file không được vượt quá 500 ký tự")
    private String fileUrl;
}
