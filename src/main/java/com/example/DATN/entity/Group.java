package com.example.DATN.entity;

import com.example.DATN.entity.enums.GroupPrivacy;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "groups_study")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 100)
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    GroupPrivacy privacy = GroupPrivacy.PRIVATE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    User owner;

    LocalDateTime createdAt = LocalDateTime.now();
    LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")


    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}