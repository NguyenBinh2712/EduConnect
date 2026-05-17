package com.example.DATN.controller;

import com.cloudinary.Api;
import com.example.DATN.dto.ApiResponse;
import com.example.DATN.dto.notification.NotificationResponse;
import com.example.DATN.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    NotificationService notificationService;

    @GetMapping
    public ApiResponse<Slice<NotificationResponse>> getMyNotifications(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ApiResponse apiResponse=new ApiResponse();
        apiResponse.setResult(notificationService.getAllMyNotification(userId, page, size));
        return apiResponse;
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> countUnread(@RequestParam Long userId) {
        ApiResponse apiResponse=new ApiResponse();
        apiResponse.setResult(notificationService.countUnread(userId));
        return apiResponse;
    }

    @PutMapping("/mark-all-read")
    public ApiResponse<String> markAllRead(@RequestParam Long userId) {
        notificationService.markAllRead(userId);
        ApiResponse apiResponse=new ApiResponse();
        apiResponse.setMessage("Đã đánh dấu tất cả là đã đọc");
        return apiResponse;
    }
}