package com.example.DATN.dto.chat;

import com.example.DATN.entity.document.Messages;
import com.example.DATN.entity.enums.TypeEvent;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequest {
    TypeEvent type;
    Long conversationId;
    Long userId;
    Messages messages;
}
