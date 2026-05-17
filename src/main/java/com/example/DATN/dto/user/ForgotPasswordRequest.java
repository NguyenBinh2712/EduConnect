package com.example.DATN.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForgotPasswordRequest {
    @Email
    @NotBlank
    String email;

    @NotBlank
    String otp;

    @NotBlank
    String newPassword;

}
