package com.example.DATN.entity.document;

import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttemptDetail {
    @Id
    String id;
    @Indexed
    Long attemptId;

    @Indexed
    Long quizId;

    @Indexed
    Long userId;

    List<Answer> answers;
    AiReview review;

    Instant createAt;

}
