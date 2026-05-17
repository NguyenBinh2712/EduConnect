package com.example.DATN.dto.notification;

import com.example.DATN.entity.enums.NotificationType;
import com.example.DATN.entity.enums.TargetType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationResponse {
    private String id;
    private Long actorId;
    private NotificationType type;
    private TargetType targetType;
    private String targetId;
    private String content;
    private boolean isRead;
    private Instant createAt;
}
