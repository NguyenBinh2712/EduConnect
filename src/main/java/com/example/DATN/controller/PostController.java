package com.example.DATN.controller;

import com.example.DATN.dto.ApiResponse;
import com.example.DATN.dto.post.*;
import com.example.DATN.entity.enums.ReactionType;
import com.example.DATN.entity.enums.ReportStatus;
import com.example.DATN.service.PostService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/post")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PostController {

    PostService postService;

    // Tạo bài post mới (hỗ trợ text + media)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse createPost(
            @RequestPart("request") @Valid PostCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        PostResponse post = postService.createPost(userId, request, files);

        ApiResponse response = new ApiResponse();
        response.setResult(post);
        response.setMessage("Tạo bài viết thành công");
        return response;
    }

    // Cập nhật bài viết (chỉ content & privacy)
    @PutMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse updatePost(
            @PathVariable Long postId,
            @RequestBody @Valid PostCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        PostResponse updated = postService.updatePost(userId, postId, request);

        ApiResponse response = new ApiResponse();
        response.setResult(updated);
        response.setMessage("Cập nhật bài viết thành công");
        return response;
    }

    // Xóa bài viết (hard delete)
    @DeleteMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        postService.deletePost(userId, postId);

        ApiResponse response = new ApiResponse();
        response.setMessage("Xóa bài viết thành công");
        return response;
    }

    // Share (chia sẻ) bài viết
    @PostMapping("/share/{originalPostId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse sharePost(
            @PathVariable Long originalPostId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        PostResponse shared = postService.sharePost(userId, originalPostId);

        ApiResponse response = new ApiResponse();
        response.setResult(shared);
        response.setMessage("Chia sẻ bài viết thành công");
        return response;
    }

    //Lấy feed công khai (phân trang)
    @GetMapping("/feed")
    public ApiResponse getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Slice<PostResponse> feed = postService.getFeed(page, size);

        ApiResponse response = new ApiResponse();
        response.setResult(feed);
        return response;
    }

    //Lấy chi tiết bài viết (bao gồm comments)
    @GetMapping("/{postId}")
    public ApiResponse getPostDetail(@PathVariable Long postId) {

        PostResponse detail = postService.getPostDetail(postId);

        ApiResponse response = new ApiResponse();
        response.setResult(detail);
        return response;
    }

    // Tạo comment hoặc reply
    @PostMapping("/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse createComment(
            @PathVariable Long postId,
            @RequestBody @Valid CommentRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        CommentResponse comment = postService.createComment(
                 postId, request, userId);

        ApiResponse response = new ApiResponse();
        response.setResult(comment);
        response.setMessage("Bình luận thành công");
        return response;
    }

    // Like / React bài viết (hoặc unlike nếu react cùng type)
    @PostMapping("/{postId}/react")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse reactToPost(
            @PathVariable Long postId,
            @RequestBody @Valid ReactionRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        PostResponse updatedPost = postService.reactToPost(postId, userId, request);

        ApiResponse response = new ApiResponse();
        response.setResult(updatedPost);
        response.setMessage("Thao tác reaction thành công");
        return response;
    }

    // Lấy reaction của chính user trên bài viết (dùng để hiển thị nút đã like chưa)
    @GetMapping("/{postId}/my-reaction")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse getMyReaction(
            @PathVariable Long postId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        ReactionType myReaction = postService.getUserReactionOnPost(postId, userId);
        ApiResponse response = new ApiResponse();
        response.setResult(myReaction);
        return response;
    }

    //Báo cáo bài viết
    @PostMapping("/{postId}/report")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse reportPost(
            @PathVariable Long postId,
            @RequestBody @Valid ReportRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long reporterId = ((Number) jwt.getClaim("userId")).longValue();

        postService.reportPost(postId, reporterId, request);

        ApiResponse response = new ApiResponse();
        response.setMessage("Báo cáo bài viết thành công. Chúng tôi sẽ xem xét sớm.");
        return response;
    }

    //Admin xử lý report (APPROVED / REJECTED)
    @PutMapping("/reports/{reportId}/handle")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse handleReport(
            @PathVariable Long reportId,
            @RequestParam ReportStatus status,
            @AuthenticationPrincipal Jwt jwt) {

        Long adminId = ((Number) jwt.getClaim("userId")).longValue();

        ReportResponse handled = postService.handleReport(reportId, status);

        ApiResponse response = new ApiResponse();
        response.setResult(handled);
        response.setMessage("Xử lý báo cáo thành công");
        return response;
    }

    //Admin lấy danh sách report đang chờ xử lý
    @GetMapping("/reports/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse getPendingReports() {

        List<ReportResponse> reports = postService.getPendingReports();

        ApiResponse response = new ApiResponse();
        response.setResult(reports);
        return response;
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        ApiResponse response = new ApiResponse();

        response.setResult(
                postService.getAdminPosts(page, size)
        );

        return response;
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        postService.deleteComment(userId, commentId);

        ApiResponse response = new ApiResponse();
        response.setMessage("Xóa bình luận thành công");
        return response;
    }

    @GetMapping("/user/{userId}")
    public ApiResponse getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) {

        Long viewerId = jwt != null ? ((Number) jwt.getClaim("userId")).longValue() : null;

        Slice<PostResponse> posts = postService.getUserPosts(userId, viewerId, page, size);

        ApiResponse response = new ApiResponse();
        response.setResult(posts);
        return response;
    }

    @GetMapping("/{postId}/comments")
    public ApiResponse<List<CommentResponse>> getCommentsByPost(
            @PathVariable Long postId
           ) {
        List<CommentResponse> comments = postService.getCommentsByPost(postId);

        ApiResponse<List<CommentResponse>> response = new ApiResponse<>();
        response.setResult(comments);
        return response;
    }
}