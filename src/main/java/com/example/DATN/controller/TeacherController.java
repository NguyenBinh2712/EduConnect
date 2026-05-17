package com.example.DATN.controller;

import com.example.DATN.dto.ApiResponse;
import com.example.DATN.dto.teacherApplication.ReviewRequest;
import com.example.DATN.dto.teacherApplication.TeacherRequest;
import com.example.DATN.dto.teacherApplication.TeacherResponse;
import com.example.DATN.entity.enums.ApplicationStatus;
import com.example.DATN.service.TeacherApplicationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/teacher")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherController {

    TeacherApplicationService teacherApplicationService;

    // User đăng ký giáo viên
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<TeacherResponse> apply(

            @RequestPart("request") @Valid TeacherRequest request,

            @RequestPart(value = "idCardFront", required = false) MultipartFile idCardFront,
            @RequestPart(value = "idCardBack", required = false) MultipartFile idCardBack,
            @RequestPart(value = "cv", required = false) MultipartFile cv,
            @RequestPart(value = "degrees", required = false) List<MultipartFile> degrees,

            @AuthenticationPrincipal Jwt jwt
    ) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        request.setIdCardFront(idCardFront);
        request.setIdCardBack(idCardBack);
        request.setCv(cv);
        request.setDegrees(degrees);

        TeacherResponse result = teacherApplicationService.apply(userId, request);

        ApiResponse<TeacherResponse> response = new ApiResponse<>();
        response.setResult(result);

        return response;
    }

    // Admin duyệt đơn
    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TeacherResponse> reviewApplication(
            @PathVariable Long id,
            @RequestBody ReviewRequest reviewRequest
    ) {

        TeacherResponse result = teacherApplicationService.reviewApplication(
                id,
                reviewRequest.getStatus(),
                reviewRequest.getReviewNote()
        );

        ApiResponse<TeacherResponse> response = new ApiResponse<>();
        response.setResult(result);

        return response;
    }

    // Admin xem danh sách đơn chờ duyệt
    @GetMapping("/applications/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<TeacherResponse>> getPendingApplications() {

        List<TeacherResponse> result = teacherApplicationService.getPending();

        ApiResponse<List<TeacherResponse>> response = new ApiResponse<>();
        response.setResult(result);

        return response;
    }

    // User xem đơn của mình
    @GetMapping("/my-application")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<TeacherResponse> getMyApplication(
            @AuthenticationPrincipal Jwt jwt
    ) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        TeacherResponse result = teacherApplicationService.getMyApplication(userId);

        ApiResponse<TeacherResponse> response = new ApiResponse<>();
        response.setResult(result);

        return response;
    }
}