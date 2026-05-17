package com.example.DATN.entity.document;

import com.example.DATN.entity.enums.NotificationType;
import com.example.DATN.entity.enums.TargetType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("notifications")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    @Id
    String id;
    Long receiverId;
    Long actorId;
    NotificationType type;
    String targetId;
    TargetType targetType ;
    String content;
    boolean isRead;

    @CreatedDate
    Instant createdAt;

}
