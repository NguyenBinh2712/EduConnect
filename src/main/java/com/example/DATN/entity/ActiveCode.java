package com.example.DATN.entity;

import com.example.DATN.entity.enums.OtpType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActiveCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false,length = 6)
    String otp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    OtpType type;

    Instant createAt;
    Instant exp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

}
