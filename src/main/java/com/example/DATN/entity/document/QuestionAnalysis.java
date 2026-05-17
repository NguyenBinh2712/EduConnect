package com.example.DATN.entity.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnalysis {
    Integer questionIndex;
    String analysis;       // AI giải thích tại sao sai
    String correctApproach;
}
