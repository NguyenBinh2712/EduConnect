package com.example.DATN.dto.friend;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendUser {
    private Long userId;
    private String email;
    private Long mutualFriendsCount;
    private Boolean isTeacher;
    private LocalDate createAt;
    private String reason;
}
