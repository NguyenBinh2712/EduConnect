package com.example.DATN.controller;

import com.example.DATN.dto.ApiResponse;
import com.example.DATN.dto.active.OtpRequest;
import com.example.DATN.dto.active.ResendOtpRequest;
import com.example.DATN.dto.user.*;
import com.example.DATN.entity.enums.OtpType;
import com.example.DATN.service.UserService;
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

@RestController
@RequestMapping("/user")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserController {
    private UserService userService;

    @PostMapping("/register")
    public ApiResponse registerUser(@RequestBody @Valid UserCreateRequest request) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResult(userService.registerUser(request));
        return apiResponse;
    }

    @PostMapping("/register/verify")
    public ApiResponse verifyOtp(@RequestBody OtpRequest request) {
        userService.verifyOtp(request);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Xác thực OTP thành công");
        return apiResponse;
    }

    @PostMapping("/register/resend-otp")
    public ApiResponse resendOtp(@RequestBody ResendOtpRequest request) {
        userService.resendOtp(request);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Gửi lại OTP thành công");
        return apiResponse;
    }

    @PutMapping("/me/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse updateMyProfile(
            @RequestBody @Valid ProfileRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId=((Number)jwt.getClaim("userId")).longValue();
        userService.createOrUpdateProfile(userId,request);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("update profile success");
        return apiResponse;
    }

    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse changeMyAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) {
        userService.changeAvatar(file);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("change avatar success");
        return apiResponse;
    }

    @PutMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse changeMyPassword(
            @RequestBody @Valid ChangePasswordRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        userService.changeMyPassword(request);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Đổi mật khẩu thành công");
        return apiResponse;
    }

    @PostMapping("/forgot-password/request-otp")
    public ApiResponse requestForgotPasswordOtp(@RequestParam String email) {
        userService.requestForgotPasswordOtp(email);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("OTP đã được gửi đến email");
        return apiResponse;
    }

    @PutMapping("/forgot-password")
    public ApiResponse forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        userService.forgotPassword(request);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Đặt lại mật khẩu thành công");
        return apiResponse;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse getMyInfo(@AuthenticationPrincipal Jwt jwt) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResult(userService.getMyInfo());
        return apiResponse;
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse getUserById(@PathVariable long id) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResult(userService.getUserById(id));
        return apiResponse;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse getAllUsers(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResult(userService.getAllUser(page,size));
        return apiResponse;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Xóa người dùng thành công");
        return apiResponse;
    }
}