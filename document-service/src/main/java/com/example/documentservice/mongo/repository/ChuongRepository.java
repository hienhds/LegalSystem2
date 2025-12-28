package com.example.documentservice.mongo.repository;

import com.example.documentservice.mongo.document.ChuongDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChuongRepository extends MongoRepository<ChuongDocument, ObjectId> {

    /**
     * Lấy danh sách chương theo de_muc_id
     * Mongo:
     * { "de_muc_id": "<deMucId>" }
     */
    @Query("{ 'de_muc_id': ?0 }")
    List<ChuongDocument> findByDeMucId(String deMucId);

    /**
     * Tìm chương theo _id (ObjectId)
     * Mongo:
     * { "_id": ObjectId("<id>") }
     */
    @Query("{ '_id': ?0 }")
    Optional<ChuongDocument> findByObjectId(ObjectId id);


    @Query("{ '_id': { $in: ?0 } }")
    List<ChuongDocument> findByIds(List<ObjectId> ids);
}
