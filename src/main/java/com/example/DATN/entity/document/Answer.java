package com.example.DATN.entity.document;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Answer {
    String questionId;
    List<Integer> selectAnswer;
    String textAnswer;                   // tự luận
    boolean isCorrect;
    Double pointsEarned; // diem thuc te
    Instant answeredAt;
}
