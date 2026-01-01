package com.example.documentservice.mongo.document;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "dieu")
@org.springframework.data.mongodb.core.index.CompoundIndexes({
        @org.springframework.data.mongodb.core.index.CompoundIndex(
                name = "idx_chuong_dieu",
                def = "{'chuong_id': 1, '_id': 1}"
        )
})
public class DieuDocument {

    @Id
    private ObjectId id; // Mongo _id

    @Field("chuong_id")
    private ObjectId chuongId; // liên kết sang chương

    @Field("tieu_de")
    private String tieuDe; // tiêu đề Điều

    @Field("ghi_chu")
    private List<GhiChu> ghiChu;

    @Field("noi_dung")
    private List<String> noiDung;

    @Field("chi_dan")
    private List<ChiDan> chiDan;

    // ================= SUB DOCUMENT =================

    @Data
    public static class GhiChu {
        private String text;
        private String link;
    }

    @Data
    public static class ChiDan {
        private String text;
        private String mapc; // để trống nếu Mongo chưa có
    }
}
