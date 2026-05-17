package com.example.DATN.dto.group;

import com.example.DATN.entity.enums.MembershipRole;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberResponse {
    private Long userId;
    private String fullName;
    private MembershipRole role;
    private LocalDateTime joinedAt;
}