package com.example.DATN.entity.document;

import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContentQuiz {
    @Id
    String id;

    Long quizId;

    String description;

    List<Question> questions;

}

