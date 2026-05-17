package com.example.DATN.dto.chat;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipantInfo {
    Long userId;
    String fullName;
    String avtUrl;
    boolean isOnline;
    LocalDateTime lastReadAt;
}
