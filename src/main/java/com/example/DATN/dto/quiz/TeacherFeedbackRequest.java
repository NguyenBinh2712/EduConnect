package com.example.DATN.dto.quiz;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeacherFeedbackRequest {
    Long attemptId;

    Integer questionIndex; // null = góp ý tổng thể

    String content;
}
