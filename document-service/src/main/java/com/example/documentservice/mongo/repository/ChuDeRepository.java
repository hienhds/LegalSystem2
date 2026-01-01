package com.example.documentservice.mongo.repository;

import com.example.documentservice.mongo.document.ChuDeDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ChuDeRepository extends MongoRepository<ChuDeDocument, ObjectId> {

    /**
     * Tìm Chủ đề theo chu_de_id (KHÔNG phải _id)
     * Mongo:
     * { "chu_de_id": "<chuDeId>" }
     */
    @Query("{ 'chu_de_id': ?0 }")
    Optional<ChuDeDocument> findByChuDeId(String chuDeId);
}
