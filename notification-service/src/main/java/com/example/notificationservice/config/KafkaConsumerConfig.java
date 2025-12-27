package com.example.notificationservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;
    private final SslBundles sslBundles;

    public KafkaConsumerConfig(
            KafkaProperties kafkaProperties,
            SslBundles sslBundles
    ) {
        this.kafkaProperties = kafkaProperties;
        this.sslBundles = sslBundles;
    }

    @Bean
    public ConsumerFactory<String, Object> notificationConsumerFactory() {

        // Spring Boot 3.2+ bắt buộc truyền SslBundles
        Map<String, Object> props =
                new HashMap<>(kafkaProperties.buildConsumerProperties(sslBundles));

        // Deserializer
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);

        // Chỉ trust package event
        props.put(JsonDeserializer.TRUSTED_PACKAGES,
                "com.example.notificationservice.event");

        // BẮT BUỘC cho nhiều event type
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);

        // An toàn: commit thủ công
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>()
        );
    }

    @Bean(name = "notificationKafkaListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object>
    notificationKafkaListenerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(notificationConsumerFactory());

        // Scale consumer
        factory.setConcurrency(3);

        // Manual ACK
        factory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }
}
