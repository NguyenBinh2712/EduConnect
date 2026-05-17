package com.example.DATN.dto.quiz;

import com.example.DATN.entity.enums.QuizStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizResponse {
    Long id;
    String title;
    String description;
    QuizStatus status;
    Long time;
    Integer maxAttempts;
    LocalDateTime startAt;
    LocalDateTime endAt;
    LocalDateTime createdAt;
    String note;
    Long creatorId;
    String creatorName;

}
