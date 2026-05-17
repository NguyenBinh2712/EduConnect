package com.example.DATN.dto.chat;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Setter
@Getter
public class UpdateMessage {
    String messageId;
    String contentNew;

}
