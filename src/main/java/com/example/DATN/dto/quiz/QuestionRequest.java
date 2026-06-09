package com.example.DATN.dto.quiz;

import com.example.DATN.entity.enums.QuestionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionRequest {

    String id;

    String questionText;
    QuestionType type;
    Double point;
    Integer order;
    String explanation;
    List<OptionRequest> options;
}