package com.example.DATN.dto.active;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OtpResponse {
    String otp;
    Instant createAt;
    Instant exp;
}
