package com.example.DATN.dto.chat;

import com.example.DATN.entity.enums.TypeEvent;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventMessages<T> {
    TypeEvent type;
    Long conversationId;
    Long userId;
    String messageId;
    T payload;
}
