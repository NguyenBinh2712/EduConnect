package com.example.DATN.dto.active;

import com.example.DATN.entity.enums.OtpType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResendOtpRequest {
    String email;
    OtpType type;
}
