package com.example.documentservice.dto;

import com.example.documentservice.mongo.document.DieuDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DieuResponse {

    private String id;
    private String tieuDe;
    private List<GhiChuResponse> ghiChu;
    private List<String> noiDung;
    private List<ChiDanResponse> chiDan;
    private ObjectId chuongId;
    private String chuDeId;
    private String deMucId;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GhiChuResponse {
        private String text;
        private String link;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChiDanResponse {
        private String mapc;
        private String text;
    }
}
