package com.example.documentservice.mongo.repository;

import com.example.documentservice.mongo.document.DieuDocument;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DieuRepository extends MongoRepository<DieuDocument, ObjectId> {

    /**
     * Lấy danh sách điều theo chuong_id
     * Mongo:
     * { "chuong_id": ObjectId("<chuongId>") }
     */
    @Query("{ 'chuong_id': ?0 }")
    List<DieuDocument> findByChuongId(ObjectId chuongId);

    /**
     * Resolve chỉ dẫn:
     * tìm DUY NHẤT 1 điều có chi_dan.text khớp
     *
     * Mongo:
     * {
     *   "chi_dan.text": { $regex: "<text>", $options: "i" }
     * }
     */
    @Query("{ 'chi_dan.text': { $regex: ?0, $options: 'i' } }")
    Optional<DieuDocument> findOneByChiDanText(String chiDanText);

    /**
     * Tìm điều theo tiêu đề chứa text cụ thể
     * VD: text = "Điều 1.6.LQ.16."
     * MongoDB: { "tieu_de": { $regex: "^\u0110iều 1\\.6\\.LQ\\.16\\.", $options: "" } }
     */
    List<DieuDocument> findByTieuDeContaining(String text);

    @Query("""
    {
      'chu_de_id': ?0,
      '_id': { $gt: ?1 }
    }
    """)
    List<DieuDocument> findByChuDeIdWithCursor(
            ObjectId chuDeId,
            ObjectId cursor,
            Pageable pageable
    );
}
