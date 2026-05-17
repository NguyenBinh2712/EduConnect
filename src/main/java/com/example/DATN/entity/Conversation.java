package com.example.DATN.entity;


import com.example.DATN.entity.enums.ConversationStatus;
import com.example.DATN.entity.enums.MembershipRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "conversation")
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;
    boolean isGroup;

     String lastMessagePreview;

     Instant lastMessageAt;

     LocalDateTime createdAt = LocalDateTime.now();

     @Enumerated(EnumType.STRING)
     @Builder.Default
    ConversationStatus status=ConversationStatus.NORMAL;

    @Column(nullable = false)
    @Builder.Default
    private boolean isPending = false;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Participant> participants = new HashSet<>();

    // Helper
    public void addParticipant(User user, MembershipRole membershipRole) {
        if(isParticipant(user.getId())) return;
        Participant p = Participant.builder()
                .conversation(this)
                .user(user)
                .membershipRole(membershipRole)
                .build();
        this.participants.add(p);
    }

    public Optional<Participant> getParticipant(Long userId) {
        return participants.stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst();
    }

    public boolean isParticipant(Long userId) {
        return participants.stream().anyMatch(p -> p.getUser().getId().equals(userId));
    }



}
