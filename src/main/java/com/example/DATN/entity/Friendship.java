package com.example.DATN.entity;

import com.example.DATN.entity.enums.FriendshipStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "friendships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "friend_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;                    // người gửi lời mời

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;                  // người nhận lời mời

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}