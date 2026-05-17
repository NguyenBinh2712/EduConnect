package com.example.DATN.dto.post;

import com.example.DATN.entity.enums.ReactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReactionRequest {
    @NotNull
    private ReactionType type;  // LIKE, LOVE, HAHA, WOW, SAD, ANGRY
}