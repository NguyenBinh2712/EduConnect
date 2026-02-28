package com.example.DATN.entity;

import com.example.DATN.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String fullName;
    String bio;
    String avatarUrl;
    String avatarPublicId;

    LocalDate birth;

    @Enumerated(EnumType.STRING)
    Gender gender;

    @OneToOne(mappedBy = "user",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    User user;

}
