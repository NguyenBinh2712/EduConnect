package com.example.DATN.dto.post;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    Long id;

    Long userId;

    String content;

    Long parentId;

    LocalDateTime createdAt;

}