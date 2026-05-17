package com.example.DATN.dto.chat;

import lombok.Data;

@Data
public class WsEditMessageRequest {
    private String messageId;
    private String content;
}
