package com.example.DATN.dto.user;

import com.example.DATN.entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    long id;
    String email;
//    boolean active;
    boolean status;
    LocalDate createAt;
    ProfileResponse profile;

}
