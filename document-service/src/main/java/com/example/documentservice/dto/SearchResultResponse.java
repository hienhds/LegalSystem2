package com.example.documentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchResultResponse {
    private String dieuId;
    private String tieuDe;
    private List<String> noiDung;
    private String chuongId;
    private String chuongText;
    private String deMucId;
    private String deMucText;
    private String chuDeId;
    private String chuDeText;
    private String highlightedText; // Đoạn text có chứa từ khóa
}
