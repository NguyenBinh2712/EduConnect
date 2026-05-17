package com.example.DATN.dto.chat;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationResponse {
    Long id;
    String name;
    String avatarUrl;
    boolean isGroup;
    String lastMessagePreview;
    Instant lastMessageAt;
    long unreadCount;
    boolean isPending;        // conversation nằm trong hàng đợi
    List<ParticipantInfo> participants;

}
