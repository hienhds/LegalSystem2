package com.example.fileservice.kafka.consumer;

import com.example.fileservice.event.FileUploadedEvent;
import com.example.fileservice.event.MinioEvent;
import com.example.fileservice.kafka.producer.FileEventProducer;
import com.example.fileservice.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUploadEventConsumer {

    private final FileMetadataRepository fileMetadataRepository;
    private final FileEventProducer fileEventProducer;

    @KafkaListener(
            topics = "minio-events",
            containerFactory = "kafkaListenerFactory"
    )
    public void consume(MinioEvent event, Acknowledgment ack) {

        if (event == null || event.getRecords() == null) {
            log.warn("⚠️ Invalid Minio event: {}", event);
            ack.acknowledge();
            return;
        }

        for (MinioEvent.Record record : event.getRecords()) {

            String rawObjectKey = record.getS3().getObject().getKey();
            String objectKey = URLDecoder.decode(rawObjectKey, StandardCharsets.UTF_8);

            log.info("🔍 Processing uploaded file: {}", objectKey);

            fileMetadataRepository.findByObjectKey(objectKey)
                    .ifPresent(metadata -> {

                        // 1️⃣ Update trạng thái
                        metadata.setStatus("COMPLETED");
                        fileMetadataRepository.save(metadata);



                        // 2️⃣ Publish event ĐẦY ĐỦ
                        FileUploadedEvent uploadedEvent =
                                FileUploadedEvent.builder()
                                        .fileId(metadata.getFileId())
                                        .bucket(metadata.getBucket())
                                        .objectKey(objectKey)
                                        .fileName(metadata.getFileName())
                                        .fileSize(metadata.getFileSize())
                                        .contentType(metadata.getContentType())
                                        .businessType(metadata.getBusinessType())
                                        .businessId(metadata.getBusinessId())
                                        .senderId(metadata.getUploadedByUserId())
                                        .build();

                        fileEventProducer.publishFileUploaded(uploadedEvent);

                        log.info(
                                "✅ Published FileUploadedEvent: fileId={}, businessType={}",
                                metadata.getFileId(),
                                metadata.getBusinessType()
                        );
                    });
        }

        ack.acknowledge();
    }
}
