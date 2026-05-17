package com.example.DATN.controller;

import com.example.DATN.dto.ApiResponse;
import com.example.DATN.dto.friend.FriendRequest;
import com.example.DATN.service.FriendshipService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/friend")
public class FriendshipController {

    FriendshipService friendshipService;

    private Long getUserId(Jwt jwt) {
        return ((Number) jwt.getClaim("userId")).longValue();
    }

    // 1. Gửi lời mời kết bạn
    @PostMapping("/request")
    public ApiResponse<String> sendRequestFriend(
            @RequestBody FriendRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        friendshipService.sendFriendRequest(getUserId(jwt), request);

        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Đã gửi lời mời kết bạn");
        return response;
    }

    // 2. Hủy lời mời
    @PostMapping("/cancel/{friendshipId}")
    public ApiResponse<String> cancelRequest(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal Jwt jwt) {

        friendshipService.cancelRequestFriend(getUserId(jwt), friendshipId);

        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Đã hủy lời mời");
        return response;
    }

    // 3. Chấp nhận lời mời
    @PostMapping("/accept/{friendshipId}")
    public ApiResponse<String> acceptRequest(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal Jwt jwt) {

        friendshipService.acceptRequestFriend(getUserId(jwt), friendshipId);

        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Đã chấp nhận lời mời");
        return response;
    }

    // 4. Từ chối lời mời
    @PostMapping("/reject/{friendshipId}")
    public ApiResponse<String> rejectRequest(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal Jwt jwt) {

        friendshipService.rejectRequestFriend(getUserId(jwt), friendshipId);

        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Đã từ chối lời mời");
        return response;
    }

    // 5. Hủy kết bạn
    @PostMapping("/unfriend")
    public ApiResponse<String> unfriend(
            @RequestBody FriendRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        friendshipService.unfriend(getUserId(jwt), request);

        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Đã hủy kết bạn");
        return response;
    }

    // 6. Danh sách lời mời đã nhận
    @GetMapping("/received")
    public ApiResponse<List<?>> getReceived(
            @AuthenticationPrincipal Jwt jwt) {

        ApiResponse<List<?>> response = new ApiResponse<>();
        response.setResult(friendshipService.getReceivedRequests(getUserId(jwt)));
        return response;
    }

    // 7. Danh sách lời mời đã gửi
    @GetMapping("/sent")
    public ApiResponse<List<?>> getSent(
            @AuthenticationPrincipal Jwt jwt) {

        ApiResponse<List<?>> response = new ApiResponse<>();
        response.setResult(friendshipService.getSentRequests(getUserId(jwt)));
        return response;
    }

    // 8. Danh sách bạn bè
    @GetMapping("/list")
    public ApiResponse<List<?>> getFriends(
            @AuthenticationPrincipal Jwt jwt) {

        ApiResponse<List<?>> response = new ApiResponse<>();
        response.setResult(friendshipService.getMyFriends(getUserId(jwt)));
        return response;
    }

    // 9. Gợi ý kết bạn
    @GetMapping("/recommend")
    public ApiResponse<List<?>> recommend(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal Jwt jwt) {

        ApiResponse<List<?>> response = new ApiResponse<>();
        response.setResult(friendshipService.getRecommendations(getUserId(jwt), limit));
        return response;
    }
}