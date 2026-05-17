package com.example.DATN.dto.quiz;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE )
public class OptionRequest {
    String text;
    boolean isCorrect;
}
