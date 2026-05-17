package com.example.DATN.dto.post;

import com.example.DATN.entity.enums.ReportReason;
import com.example.DATN.entity.enums.ReportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {
    private Long id;
    private Long postId;
    private Long reporterId;
    private ReportReason reason;
    private String description;
    private ReportStatus status;
    private LocalDateTime reportedAt;
}