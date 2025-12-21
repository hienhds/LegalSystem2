package com.example.fileservice.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "file_metadata")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataDocument {

    @Id
    private String fileId;

    private String fileName;
    private String contentType;
    private Long fileSize;

    private Long uploadedByUserId;

    private String bucket;
    private String objectKey;

    private String businessType; // CHAT_AVATAR, MESSAGE_FILE
    private String businessId;   // conversationId, messageId

    private Instant uploadedAt;
    private String status; // PENDING | COMPLETED
}
