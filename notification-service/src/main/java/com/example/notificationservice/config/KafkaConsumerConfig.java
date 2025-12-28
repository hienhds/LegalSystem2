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
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
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
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(sslBundles));

        // 1. Cấu hình JsonDeserializer thủ công
        JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>();
        jsonDeserializer.addTrustedPackages("*");

        // Cấu hình Type Mapping ngay trong Java để khớp các Event từ Chat Service
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);

        Map<String, Class<?>> idClassMapping = new HashMap<>();
        // Thêm tất cả các Event của bạn vào đây
        idClassMapping.put("com.example.chatservice.event.ConversationCreatedEvent", com.example.notificationservice.event.ConversationCreatedEvent.class);
        idClassMapping.put("conversation-created", com.example.notificationservice.event.ConversationCreatedEvent.class);
        idClassMapping.put("invite-created", com.example.notificationservice.event.InviteEvent.class);
        idClassMapping.put("member-removed", com.example.notificationservice.event.RemoveMemberEvent.class);
        idClassMapping.put("group-dissolved", com.example.notificationservice.event.DissolveGroupEvent.class);
        idClassMapping.put("conversation-decline", com.example.notificationservice.event.ConversationInviteDeclinedEvent.class);

        typeMapper.setIdClassMapping(idClassMapping);
        jsonDeserializer.setTypeMapper(typeMapper);

        // 2. Bọc vào ErrorHandlingDeserializer và truyền trực tiếp vào constructor
        ErrorHandlingDeserializer<Object> valueDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);
        ErrorHandlingDeserializer<String> keyDeserializer = new ErrorHandlingDeserializer<>(new StringDeserializer());

        return new DefaultKafkaConsumerFactory<>(props, keyDeserializer, valueDeserializer);
    }

    @Bean(name = "notificationKafkaListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> notificationKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationConsumerFactory());
        factory.setConcurrency(3);

        // Manual Ack mode
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }
}
