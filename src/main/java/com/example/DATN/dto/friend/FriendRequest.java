package com.example.DATN.dto.friend;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequest {
    private Long targetUserId;
}