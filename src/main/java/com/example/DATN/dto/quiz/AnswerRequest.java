package com.example.DATN.dto.quiz;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnswerRequest {
    Integer questionIndex;
    List<Integer> selectedOptionIndexes; // trắc nghiệm
    String textAnswer;
}
