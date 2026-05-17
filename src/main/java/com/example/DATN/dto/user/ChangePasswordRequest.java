package com.example.DATN.dto.user;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {
    String oldPassword;
    String newPassword;
}
