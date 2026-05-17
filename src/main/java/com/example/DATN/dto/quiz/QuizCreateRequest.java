package com.example.DATN.dto.quiz;

import com.example.DATN.entity.Group;
import com.example.DATN.entity.User;
import com.example.DATN.entity.document.Question;
import com.example.DATN.entity.enums.QuizStatus;
import jakarta.persistence.*;
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

    Long userId;
    Long groupId;

    String title;
    String description;
    QuizStatus status;
    Long time;
    Integer maxAttempt=3;
    LocalDateTime startAt;
    LocalDateTime endAt;
    boolean allowAiReview;
    List<Question> questions;


}
