package com.example.DATN.dto.teacherApplication;

import com.example.DATN.entity.enums.ApplicationStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {
    private ApplicationStatus status;

    private String reviewNote;
}
