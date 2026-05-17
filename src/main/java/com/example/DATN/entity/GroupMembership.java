package com.example.DATN.entity;

import com.example.DATN.entity.enums.MembershipRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_memberships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "user_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    MembershipRole role = MembershipRole.MEMBER;

    LocalDateTime joinedAt = LocalDateTime.now();
}