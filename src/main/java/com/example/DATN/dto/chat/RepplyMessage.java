package com.example.DATN.dto.chat;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RepplyMessage {
    String parentMessageId;
    SendMessageRequest sendRequest;
}
