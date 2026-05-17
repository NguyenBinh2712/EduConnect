package com.example.DATN.entity;

import com.example.DATN.entity.enums.ReactionTargetType;
import com.example.DATN.entity.enums.ReactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reactions",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"target_id", "target_type", "user_id"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // id của post hoặc comment
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    // POST hoặc COMMENT
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ReactionTargetType targetType;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private ReactionType type;

    private LocalDateTime createdAt = LocalDateTime.now();
}