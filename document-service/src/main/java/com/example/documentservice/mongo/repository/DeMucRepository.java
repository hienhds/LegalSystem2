package com.example.documentservice.mongo.repository;

import com.example.documentservice.mongo.document.DeMucDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DeMucRepository extends MongoRepository<DeMucDocument, ObjectId> {

    /**
     * Lấy danh sách đề mục theo chu_de_id
     * Mongo:
     * { "chu_de_id": "<chuDeId>" }
     */
    @Query("{ 'chu_de_id': ?0 }")
    List<DeMucDocument> findByChuDeId(String chuDeId);

    /**
     * Tìm DUY NHẤT 1 đề mục theo de_muc_id
     * Mongo:
     * { "de_muc_id": "<deMucId>" }
     */
    @Query("{ 'de_muc_id': ?0 }")
    Optional<DeMucDocument> findByDeMucId(String deMucId);

    @Query("{ '_id': { $in: ?0 } }")
    List<DeMucDocument> findByIds(List<ObjectId> ids);
}
