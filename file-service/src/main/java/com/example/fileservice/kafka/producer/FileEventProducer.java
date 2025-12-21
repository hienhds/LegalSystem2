package com.example.fileservice.kafka.producer;

import com.example.fileservice.event.FileUploadedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishFileUploaded(FileUploadedEvent event){
        kafkaTemplate.send("file-uploaded", event.getFileId(), event);
    }

}
