package com.example.chatservice.kafka;

import com.example.chatservice.event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ChatEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishGroupCreated(ConversationCreatedEvent event) {
        kafkaTemplate.send("conversation-created", event);
    }

    public void publishInviteCreated(InviteEvent event) {
        kafkaTemplate.send("invite-created", event);
    }

    public void publishRemoveMember(RemoveMemberEvent event) {
        kafkaTemplate.send("member-removed", event);

    }

    public void publishDissolveGroup(DissolveGroupEvent event) {
        kafkaTemplate.send("group-dissolved", event.getConversationName(), event);
    }

    // chap nhan lời mời

    public void publishConversationJoined(ConversationJoinedEvent event) {
        kafkaTemplate.send(
                "conversation-joined",
                event.getConversationId(),
                event
        );
    }

    public void publishDeclineInvite(ConversationInviteDeclinedEvent event){
        kafkaTemplate.send("conversation-decline",
                event);
    }

    public void publishMessageCreated(MessageEvent event){
        kafkaTemplate.send("conversation-message-created", event.getConversationId(), event);
    }

}