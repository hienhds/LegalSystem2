package com.example.documentservice.dto;

import com.example.documentservice.mongo.document.DieuDocument;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TreeChuDeItem {

    private SimpleNode deMuc;
    private SimpleNode chuong;
    private DieuContent dieu;

    @Data
    @Builder
    public static class SimpleNode {
        private String id;
        private String text;
    }

    @Data
    @Builder
    public static class DieuContent {
        private String id;
        private String tieuDe;
        private List<String> noiDung;
        private List<DieuDocument.GhiChu> ghiChu;
        private List<DieuDocument.ChiDan> chiDan;
    }
}
