package com.example.DATN.entity;

import com.example.DATN.entity.enums.AttemptStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    Quiz quiz;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "group_id")
//    Group group;

    Integer attemptNumber;

    Double score;
    Double scorePrecent;
    Double totalPoints;
    @Enumerated(EnumType.STRING)
    AttemptStatus status;

    LocalDateTime startAt;
    LocalDateTime submitAt;

    @Builder.Default()
    boolean AiReviewRequest=false;

    String detailQuizId;
}
