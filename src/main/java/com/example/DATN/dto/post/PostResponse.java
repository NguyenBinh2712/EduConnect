package com.example.DATN.dto.post;

import com.example.DATN.entity.Comment;
import com.example.DATN.entity.enums.PostType;
import com.example.DATN.entity.enums.Privacy;
import com.example.DATN.entity.enums.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private Long id;

    private Long userId;

    private String content;

    private Privacy privacy;

    private PostType postType;

    private Long originalPostId;

    List<CommentResponse> comments;

    private List<PostMediaDto> medias;

    // reaction count
    private Map<ReactionType, Long> reactions;

    // tổng comment
    private Long commentCount;

    private boolean isHidden;

    private LocalDateTime createdAt;

}