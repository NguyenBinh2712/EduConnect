package com.example.DATN.dto.chat;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMessageRequest {
    private Long conversationId;
    private Long targetUserId;

    private String content;

}
