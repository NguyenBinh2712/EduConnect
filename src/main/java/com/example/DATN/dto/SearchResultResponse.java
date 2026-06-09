package com.example.DATN.dto;

import com.example.DATN.dto.group.GroupResponse;
import com.example.DATN.dto.post.PostResponse;
import com.example.DATN.dto.user.UserResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchResultResponse {
    List<UserResponse> users;
    List<PostResponse> posts;
    List<GroupResponse> groups;
}