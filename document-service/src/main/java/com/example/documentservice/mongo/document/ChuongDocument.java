package com.example.documentservice.mongo.document;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "chuong")
@org.springframework.data.mongodb.core.index.CompoundIndexes({
        @org.springframework.data.mongodb.core.index.CompoundIndex(
                name = "idx_demuc_chuong",
                def = "{'de_muc_id': 1}"
        )
})
public class ChuongDocument {

    @Id
    private ObjectId id;

    @Field("de_muc_id")
    private String deMucId;   // liên kết đề mục

    @Field("text")
    private String text;      // tên chương
}
