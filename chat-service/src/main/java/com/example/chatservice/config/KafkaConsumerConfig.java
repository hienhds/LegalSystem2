package com.example.chatservice.config;

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

    /**
     * ConsumerFactory dùng chung cho TẤT CẢ event
     */
    @Bean
    public ConsumerFactory<String, Object> kafkaConsumerFactory() {

        Map<String, Object> props =
                new HashMap<>(kafkaProperties.buildConsumerProperties(sslBundles));

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES,
                "*");

        props.put(JsonDeserializer.TYPE_MAPPINGS,
                "com.example.fileservice.event.FileUploadedEvent:com.example.chatservice.event.FileUploadedEvent");

        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>()
        );
    }

    /**
     * Listener factory – mở rộng cho nhiều event sau này
     */
    @Bean(name = "kafkaListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object>
    kafkaListenerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(kafkaConsumerFactory());

        // scale sau này
        factory.setConcurrency(3);

        // manual ack an toàn
        factory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }
}
