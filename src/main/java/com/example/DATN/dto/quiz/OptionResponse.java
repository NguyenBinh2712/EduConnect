package com.example.DATN.dto.quiz;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OptionResponse {
    Integer index;
    String text;

}
