package com.example.DATN.entity.document;

import com.example.DATN.entity.enums.QuestionType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Question {
    String id;
    String questionText;
    QuestionType type;
    Double point;

    Integer order;
    String explanation;
   List<QuestionOption> options;

}
