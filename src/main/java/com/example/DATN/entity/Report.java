package com.example.DATN.entity;

import com.example.DATN.entity.enums.ReportReason;
import com.example.DATN.entity.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "reports")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "reporter_id")
    private Long reporterId;

    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING;

    private LocalDateTime reportedAt = LocalDateTime.now();

    private LocalDateTime handledAt;

}