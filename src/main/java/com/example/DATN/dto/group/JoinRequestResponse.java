package com.example.DATN.dto.group;

import com.example.DATN.entity.enums.JoinRequestStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequestResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long inviterId;
    private String inviterName;
    private JoinRequestStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
}