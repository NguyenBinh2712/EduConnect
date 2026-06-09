package com.example.DATN.controller;

import com.example.DATN.dto.ApiResponse;
import com.example.DATN.dto.notification.NotificationResponse;
import com.example.DATN.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    NotificationService notificationService;

    @GetMapping
    public ApiResponse<Slice<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        ApiResponse<Slice<NotificationResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(
                notificationService.getAllMyNotification(userId, page, size)
        );

        return apiResponse;
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> countUnread(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        ApiResponse<Long> apiResponse = new ApiResponse<>();
        apiResponse.setResult(notificationService.countUnread(userId));

        return apiResponse;
    }

    @PutMapping("/mark-all-read")
    public ApiResponse<String> markAllRead(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        notificationService.markAllRead(userId);

        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Đã đánh dấu tất cả là đã đọc");

        return apiResponse;
    }
}