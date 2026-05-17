package com.example.DATN.dto.post;

import com.example.DATN.entity.enums.PostType;
import com.example.DATN.entity.enums.Privacy;
import lombok.Data;

@Data
public class PostCreateRequest {

    private String content;

    private Privacy privacy=Privacy.PUBLIC;

    private Long originalPostId; // nếu share post
}