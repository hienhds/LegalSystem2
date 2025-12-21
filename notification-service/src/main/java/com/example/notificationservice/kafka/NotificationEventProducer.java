package com.example.notificationservice.kafka;
import com.example.notificationservice.event.NotificationEvent;
import com.example.notificationservice.event.UnreadCountEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class NotificationEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishNotificationCreated(NotificationEvent event){
        kafkaTemplate.send("notification-created", event);
    }

    public void publishUnreadCountNotification(UnreadCountEvent event){
        kafkaTemplate.send("notification-count", event);
    }


}
