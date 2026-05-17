package com.example.DATN.dto.post;

import com.example.DATN.entity.enums.ReactionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReactionResponse {
    private ReactionType type;
    private Long userId;
}