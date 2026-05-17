package com.example.DATN.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {


    private Long parentId; // null nếu là comment gốc

    @NotBlank(message = "Content không được để trống")
    private String content;
}