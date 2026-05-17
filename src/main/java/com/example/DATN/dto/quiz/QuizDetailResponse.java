package com.example.DATN.dto.quiz;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizDetailResponse {
    Long id;
    String title;
    String description;
    Long time;
    Integer maxAttempts;
    boolean allowAiReview;
    LocalDateTime startAt;
    LocalDateTime endAt;
    List<QuestionResponse> questions;
}
