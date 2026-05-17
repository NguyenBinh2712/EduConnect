package com.example.DATN.dto.quiz;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnswerResponse {
    String  questionId;
    String questionText;
    List<Integer> selectedOptionIndexes;
    String textAnswer;
    boolean isCorrect;
    Double pointsEarned;
    Double maxPoints;
    String explanation; // giải thích đáp án đúng từ QuizContent
}
