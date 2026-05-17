package com.example.DATN.dto.group;

import com.example.DATN.entity.enums.MembershipRole;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleRequest {
    private MembershipRole newRole;
}