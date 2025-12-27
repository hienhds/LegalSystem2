package com.example.fileservice.repository;

import com.example.fileservice.document.FileMetadataDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FileMetadataRepository extends MongoRepository<FileMetadataDocument, String> {
    Optional<FileMetadataDocument> findByFileId(String fileId);
    Optional<FileMetadataDocument> findByObjectKey(String objectKey);
}

