package com.example.DATN.entity;

import com.example.DATN.entity.enums.FriendshipStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String email;

    String password;

    LocalDate createAt;
    boolean status;
    boolean active;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Role> roles;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ActiveCode> activeCodes;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Profile profile;

    @Builder.Default
    private boolean isTeacher = false;

    private LocalDateTime teacherVerifiedAt;

    // friend
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Friendship> sentFriendships = new HashSet<>();

    @OneToMany(mappedBy = "friend", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Friendship> receivedFriendships = new HashSet<>();

    @Transient
    public Set<User> getFriends() {
        Set<User> friends = new HashSet<>();

        sentFriendships.stream()
                .filter(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                .map(Friendship::getFriend)
                .forEach(friends::add);

        receivedFriendships.stream()
                .filter(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                .map(Friendship::getUser)
                .forEach(friends::add);

        return friends;
    }


}
