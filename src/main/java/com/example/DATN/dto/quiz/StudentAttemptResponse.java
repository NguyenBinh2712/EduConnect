package com.example.DATN.dto.quiz;

import com.example.DATN.entity.enums.AttemptStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentAttemptResponse {
    Long attemptId;
    Long userId;
    String studentName;
    String studentAvatar;
    Integer attemptNumber;
    AttemptStatus status;
    Double score;
    Double scorePercent;
    LocalDateTime submittedAt;
    boolean aiReviewRequested;
    // Giáo viên có thể xem chi tiết từng câu + thêm feedback
    List<AnswerResponse> answers;
    List<FeedbackResponse> feedbacks;
}
