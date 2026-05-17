package com.example.DATN.dto.quiz;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiReviewResponse {
    String overallAnalysis;
    List<String> weaknessAreas;
    String studyRoadmap;
    List<QuestionResponse> perQuestion;
    Instant generatedAt;
}
