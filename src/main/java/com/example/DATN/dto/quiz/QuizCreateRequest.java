package com.example.DATN.dto.quiz;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizCreateRequest {

    Long groupId;
    String title;
    String description;
    Long time;
    @Builder.Default
    Integer maxAttempt = 3;
    LocalDateTime startAt;
    LocalDateTime endAt;
    boolean allowAiReview;

    List<QuestionRequest> questions;
}