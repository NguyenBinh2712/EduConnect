package com.example.DATN.service;

import com.example.DATN.dto.notification.NotificationResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class NotificationRealTimeService {
    SimpMessagingTemplate simpMessagingTemplate;

    static final String DESTINATION="/queue/notifications";

    public void pushToUser(Long userId, NotificationResponse notification){
        simpMessagingTemplate.convertAndSendToUser(userId.toString(),DESTINATION,notification);
    }

    public void pushToUser(Long userId, List<NotificationResponse> notifications) {
        simpMessagingTemplate.convertAndSendToUser(
                userId.toString(), DESTINATION, notifications);
    }

    public void broadcast(NotificationResponse notification) {
        simpMessagingTemplate.convertAndSend(
                "/topic/notifications",
                notification
        );
    }
}
