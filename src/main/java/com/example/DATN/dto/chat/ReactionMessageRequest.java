package com.example.DATN.dto.chat;

import com.example.DATN.entity.enums.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactionMessageRequest {
    String messageId;
    ReactionType type;
}
