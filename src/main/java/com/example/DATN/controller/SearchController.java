package com.example.DATN.controller;

import com.example.DATN.dto.ApiResponse;
import com.example.DATN.dto.SearchResultResponse;
import com.example.DATN.dto.group.GroupResponse;
import com.example.DATN.dto.post.PostResponse;
import com.example.DATN.dto.user.UserResponse;
import com.example.DATN.service.GroupService;
import com.example.DATN.service.PostService;
import com.example.DATN.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("isAuthenticated()")
public class SearchController {

    UserService userService;
    PostService postService;
    GroupService groupService;

    // Tìm kiếm người dùng theo tên hoặc email
    @GetMapping("/users")
    public ApiResponse<Slice<UserResponse>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ApiResponse<Slice<UserResponse>> response = new ApiResponse<>();
        response.setResult(userService.searchUsers(keyword, page, size));
        return response;
    }

    // Tìm kiếm bài viết theo nội dung (chỉ post công khai, không thuộc group)
    @GetMapping("/posts")
    public ApiResponse<Slice<PostResponse>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ApiResponse<Slice<PostResponse>> response = new ApiResponse<>();
        response.setResult(postService.searchPosts(keyword, page, size));
        return response;
    }

    // Tìm kiếm nhóm theo tên
    @GetMapping("/groups")
    public ApiResponse<List<GroupResponse>> searchGroups(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {

        List<GroupResponse> groups = groupService.searchGroups(keyword);
        ApiResponse<List<GroupResponse>> response = new ApiResponse<>();
        response.setResult(groups.subList(0, Math.min(groups.size(), limit)));
        return response;
    }

    // Tìm kiếm tổng hợp — trả về cả 3 loại cùng lúc
    @GetMapping
    public ApiResponse<SearchResultResponse> searchAll(
            @RequestParam String keyword,
            @AuthenticationPrincipal Jwt jwt) {

        Slice<UserResponse> users = userService.searchUsers(keyword, 0, 5);
        Slice<PostResponse> posts = postService.searchPosts(keyword, 0, 5);
        List<GroupResponse> groups = groupService.searchGroups(keyword)
                .stream().limit(5).toList();

        ApiResponse<SearchResultResponse> response = new ApiResponse<>();
        response.setResult(SearchResultResponse.builder()
                .users(users.getContent())
                .posts(posts.getContent())
                .groups(groups)
                .build());
        return response;
    }
}