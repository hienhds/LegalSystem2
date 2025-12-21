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

        for (MinioEvent.Record record : event.getRecords()) {

            String objectKey =
                    record.getS3().getObject().getKey();

            fileMetadataRepository.findByObjectKey(objectKey)
                    .ifPresent(metadata -> {

                        metadata.setStatus("COMPLETED");
                        fileMetadataRepository.save(metadata);

                        fileEventProducer.publishFileUploaded(
                                FileUploadedEvent.builder()
                                        .fileId(metadata.getFileId())
                                        .bucket(metadata.getBucket())
                                        .objectKey(objectKey)
                                        .businessType(metadata.getBusinessType())
                                        .businessId(metadata.getBusinessId())
                                        .contentType(metadata.getContentType())
                                        .build()
                        );

                        log.info("✅ File uploaded: {}", objectKey);
                    });
        }

        ack.acknowledge();
    }
}
