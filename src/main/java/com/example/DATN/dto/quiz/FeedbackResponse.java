package com.example.DATN.dto.quiz;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackResponse {
    Long id;
    Long teacherId;
    String teacherName;
    String teacherAvatar;
    String questionId; // null = tổng thể
    String content;
    LocalDateTime createdAt;
}
