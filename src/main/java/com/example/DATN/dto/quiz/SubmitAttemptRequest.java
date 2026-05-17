package com.example.DATN.dto.quiz;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmitAttemptRequest {
    Long attemptId;
    List<AnswerRequest> answers;

}
