package com.example.documentservice.mongo.document;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "de_muc")
@org.springframework.data.mongodb.core.index.CompoundIndexes({
        @org.springframework.data.mongodb.core.index.CompoundIndex(
                name = "idx_chude_demuc",
                def = "{'chu_de_id': 1}"
        ),
        @org.springframework.data.mongodb.core.index.CompoundIndex(
                name = "idx_demuc_id",
                def = "{'de_muc_id': 1}"
        )
})
public class DeMucDocument {


    @Id
    private ObjectId id;

    @Field("de_muc_id")
    private String deMucId; // đúng với MongoDB

    @Field("text")
    private String text;

    @Field("chu_de_id")
    private String chuDeId; // reference thủ công
}