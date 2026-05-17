package com.example.DATN.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Entity
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackTeacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    User teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Attempt_id")
    QuizAttempt attempt;

    String questionId;

    String content;

    LocalDateTime createAt;

}
