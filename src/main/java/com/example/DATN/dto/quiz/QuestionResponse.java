package com.example.DATN.dto.quiz;

import com.example.DATN.entity.enums.QuestionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionResponse {
    String id;
    String questionText;
    QuestionType type;
    Double points;
    String mediaUrl;
    String explanation;
    List<OptionResponse> options;
}
