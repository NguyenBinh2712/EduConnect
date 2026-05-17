package com.example.DATN.entity;

import com.example.DATN.entity.enums.MembershipRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "participant",
        uniqueConstraints = @UniqueConstraint(columnNames = {"conversation_id", "user_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
     Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
     User user;

    LocalDateTime joinAt= LocalDateTime.now();
    LocalDateTime lastReadAt;

    @Enumerated(EnumType.STRING)
    MembershipRole membershipRole;

    @Builder.Default
    boolean deleted=false;
    Instant deleteAt;

    @Builder.Default
    boolean archived=false;

}
