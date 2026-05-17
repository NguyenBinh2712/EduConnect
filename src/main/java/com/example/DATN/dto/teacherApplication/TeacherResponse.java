package com.example.DATN.dto.teacherApplication;
import com.example.DATN.entity.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TeacherResponse {
    private Long id;
    private Long applicantId;
    private String applicantEmail;
    private String reason;
    private String idCardFrontUrl;
    private String idCardBackUrl;
    private String degreeUrlsJson; // hoặc List<String> nếu parse json
    private String cvUrl;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    private String reviewNote;
}
