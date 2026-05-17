package com.example.DATN.dto.user;

import com.example.DATN.entity.enums.Gender;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileRequest {
    String fullName;
    String bio;
    String avatar;
    String avatarPublicId;
    LocalDate birth;
    Gender gender;
}
