package com.example.DATN.controller;

import com.example.DATN.dto.ApiResponse;
import com.example.DATN.service.BlockService;
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
@RequestMapping("/block")
public class BlockController {

    BlockService blockService;

    @PostMapping("/{blockedId}")
    public ApiResponse<String> blockUser(@AuthenticationPrincipal Jwt jwt,
                                         @PathVariable Long blockedId) {
        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        blockService.blockUser(userId, blockedId);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Block user success");
        return response;
    }

    @DeleteMapping("/{blockedId}")
    public ApiResponse<String> unBlock(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable Long blockedId) {
        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        blockService.unblockUser(userId, blockedId);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Unblock user success");
        return response;
    }

    @GetMapping
    public ApiResponse<List<Long>> getMyBlocked(@AuthenticationPrincipal Jwt jwt) {
        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        ApiResponse<List<Long>> response = new ApiResponse<>();
        response.setResult(blockService.getMyBlocked(userId));
        return response;
    }

    @GetMapping("/check/{targetId}")
    public ApiResponse<Boolean> isBlocked(@AuthenticationPrincipal Jwt jwt,
                                          @PathVariable Long targetId) {
        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        ApiResponse<Boolean> response = new ApiResponse<>();
        response.setResult(blockService.isBlocked(userId, targetId));
        return response;
    }
}

