package com.example.DATN.dto.post;

import com.example.DATN.entity.enums.Privacy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AdminPostItemResponse {
    Long id;
    UserPost user;
    String authorName;
    String authorAvatar;

    String content;

    Privacy privacy;

    Long reportCount;

    LocalDateTime createdAt;
}
