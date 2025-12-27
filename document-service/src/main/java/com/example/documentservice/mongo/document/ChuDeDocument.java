package com.example.documentservice.mongo.document;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "chu_de")
public class ChuDeDocument {

    @Id
    private ObjectId id;
    @Field("chu_de_id")
    @org.springframework.data.mongodb.core.index.Indexed(unique = true)
    private String chuDeId; // chu_de_id

    private String text;
}