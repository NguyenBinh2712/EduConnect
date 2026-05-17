package com.example.DATN.controller;

import com.example.DATN.dto.ApiResponse;
import com.example.DATN.dto.auth.*;
import com.example.DATN.service.AuthService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthController {
    AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @RequestBody @Valid AuthRequest request) {

        ApiResponse apiResponse=new ApiResponse();
        apiResponse.setResult(authService.login(request));
        return apiResponse;
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(
            @RequestBody LogoutRequest request) {

        authService.logout(request);
        ApiResponse apiResponse=new ApiResponse();
        apiResponse.setMessage("login success");
        return apiResponse;
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refreshToken(
            @RequestBody RefreshRequest request) throws Exception {

        ApiResponse apiResponse=new ApiResponse();
        apiResponse.setResult(authService.refreshToken(request));
        return apiResponse;
    }


    @PostMapping("/introspect")
    public ApiResponse<Boolean> introspect(
            @RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result=authService.introspect(request);
        ApiResponse apiResponse=new ApiResponse();
        apiResponse.setResult(result);
        return apiResponse;
    }
}
