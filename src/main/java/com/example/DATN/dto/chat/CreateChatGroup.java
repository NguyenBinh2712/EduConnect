package com.example.DATN.dto.chat;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Setter
@Getter
public class CreateChatGroup {
    String name;
    List<Long> memberId;
}

