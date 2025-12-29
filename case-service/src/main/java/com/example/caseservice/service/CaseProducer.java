package com.example.caseservice.service;

import com.example.caseservice.dto.CaseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseProducer {
    private final KafkaTemplate<String, String> kafkaTemplate; // Chuyển sang String để khớp config
    private final ObjectMapper objectMapper; // Dùng để convert Object sang JSON String
    private static final String TOPIC = "case-sync-topic";

    public void sendCaseEvent(CaseEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            log.info("Gửi sự kiện vụ án sang Kafka: {}", event.getId());
            kafkaTemplate.send(TOPIC, String.valueOf(event.getId()), message);
        } catch (Exception e) {
            log.error("LỖI KHI SERIALIZE EVENT KAFKA: {}", e.getMessage());
        }
    }
}