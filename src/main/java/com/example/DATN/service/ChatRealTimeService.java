package com.example.DATN.service;

import com.example.DATN.config.WebSocketEventListener;
import com.example.DATN.dto.chat.EventMessages;
import com.example.DATN.entity.document.Messages;
import com.example.DATN.entity.enums.TypeEvent;
import com.example.DATN.repository.ParticipantRepository;
import jakarta.transaction.Transactional;
import jdk.jfr.EventType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ChatRealTimeService {
    SimpMessagingTemplate simpMessagingTemplate;
    ParticipantRepository participantRepository;

    public void sendToConversation(Long convId,EventMessages<?> event){
        simpMessagingTemplate.convertAndSend("/topic/conversation."+convId,event);
    }

    public void sendToUser(Long userId,String destination,EventMessages<?> event){
        simpMessagingTemplate.convertAndSendToUser(String.valueOf(userId),destination,event);
    }
    public void sendNotification(Long userId,EventMessages<?> event){
        sendToUser(userId,"/queue/notification",event);
    }

    public void pushToOffline(Long convId, Long senderId, Messages msg){
        List<Long> users=participantRepository.findUserIdsByConversationId(convId);
        users.stream()
                .filter(id-> !senderId.equals(id))
                .filter(id-> !WebSocketEventListener.isOnline(id))
                .forEach(uid -> {

                    EventMessages<Map<String, Object>> event =
                            EventMessages.<Map<String, Object>>builder()
                                    .type(TypeEvent.MESSAGE)
                                    .conversationId(convId)
                                    .userId(senderId)
                                    .messageId(msg.getId())
                                    .payload(Map.of(
                                            "preview", msg.getContent()
                                    ))
                                    .build();

                    sendNotification(uid, event);
                });
    }
}
