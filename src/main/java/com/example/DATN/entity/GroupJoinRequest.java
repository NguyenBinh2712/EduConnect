package com.example.DATN.entity;

import com.example.DATN.entity.enums.JoinRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_join_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id")
    User inviter;  // nếu được mời

    @Enumerated(EnumType.STRING)
    JoinRequestStatus status = JoinRequestStatus.PENDING;

    LocalDateTime requestedAt = LocalDateTime.now();
    LocalDateTime reviewedAt;
}