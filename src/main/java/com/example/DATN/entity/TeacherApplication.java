package com.example.DATN.entity;

import com.example.DATN.entity.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "teacher_applications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    private String idCardFrontUrl;
    private String idCardBackUrl;

    @Column(columnDefinition = "TEXT")
    private String degreeUrlsJson;

    private String cvUrl;  // link hoặc URL upload

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    private LocalDateTime appliedAt = LocalDateTime.now();
    private LocalDateTime reviewedAt;

    @Column(columnDefinition = "TEXT")
    private String reviewNote;


}