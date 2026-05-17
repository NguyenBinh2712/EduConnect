package com.example.DATN.dto.quiz;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApproveQuiz {
    boolean approved;
    String note;
}
