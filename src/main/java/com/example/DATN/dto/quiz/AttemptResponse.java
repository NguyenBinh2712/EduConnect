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
public class AttemptResponse {
    Long attemptId;
    Long studentId;
    String studentName;
    String studentAvatar;
    Integer attemptNumber;
    AttemptStatus status;
    Double score;
    Double scorePercent;
    Double totalPoints;
    LocalDateTime submittedAt;
    boolean aiReview;
    Double bestScore;          // điểm cao nhất trong tất cả lần làm
    boolean canRetake;
    List<FeedbackResponse> feedbacks;
    List<AnswerResponse> answers;

}
