package com.example.DATN.entity;

import com.example.DATN.entity.enums.QuizStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    Group group;

    String title;
    String description;

    @Enumerated(EnumType.STRING)
    QuizStatus status;

    Long time;

    @Builder.Default
    Integer maxAttempt = 3;

    @Column(columnDefinition = "TEXT")
    String note;

    LocalDateTime startAt;
    LocalDateTime endAt;
    LocalDateTime createAt;

    String contentQuizId;
    boolean allowAiReview;

    @PrePersist
    void onCreate() {
        createAt = LocalDateTime.now();
    }
}