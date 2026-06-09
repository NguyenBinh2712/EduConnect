package com.example.DATN.dto.post;

import com.example.DATN.entity.enums.PostType;
import com.example.DATN.entity.enums.Privacy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCreateRequest {

    private String content;

    private Privacy privacy=Privacy.PUBLIC;
    private Long groupId;

    private Long originalPostId; // nếu share post
}