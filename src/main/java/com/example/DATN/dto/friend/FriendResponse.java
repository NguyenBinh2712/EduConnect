package com.example.DATN.dto.friend;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendResponse {
    private Long friendshipId;
    private Long userId;
    private String fullName;
    private LocalDateTime since;
}