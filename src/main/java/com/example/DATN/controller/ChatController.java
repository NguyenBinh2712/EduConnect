package com.example.DATN.controller;

import com.example.DATN.dto.ApiResponse;
import com.example.DATN.dto.chat.*;
import com.example.DATN.service.ChatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/chat")
public class ChatController {

    ChatService chatService;

    @PostMapping(value = "/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MessageResponse> sendMessage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("request") SendMessageRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        ApiResponse<MessageResponse> response = new ApiResponse<>();
        response.setResult(chatService.sendMessages(userId, request, files));
        return response;
    }

    @GetMapping("/messages/{convId}")
    public ApiResponse<Slice<MessageResponse>> getMessages(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long convId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        ApiResponse<Slice<MessageResponse>> response = new ApiResponse<>();
        response.setResult(chatService.getMessages(convId, userId, page, size));
        return response;
    }

    @PutMapping("/messages/{convId}/seen")
    public ApiResponse<String> markSeen(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long convId) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        chatService.markMessages(convId, userId);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Marked as seen");
        return response;
    }

    @PostMapping("/messages/reaction")
    public ApiResponse<String> reactionMessage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ReactionMessageRequest request) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        chatService.reactionMessage(userId, request);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Reaction saved");
        return response;
    }

    @PostMapping("/messages/reply")
    public ApiResponse<MessageResponse> replyMessage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody RepplyMessage request) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        ApiResponse<MessageResponse> response = new ApiResponse<>();
        response.setResult(chatService.replyMessage(userId, request));
        return response;
    }

    @PatchMapping("/messages/{messageId}")
    public ApiResponse<MessageResponse> editMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String messageId,
            @RequestBody EditMessageRequest request) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        ApiResponse<MessageResponse> response = new ApiResponse<>();
        response.setResult(chatService.editMessage(userId, messageId, request.getContent()));
        return response;
    }

    @DeleteMapping("/messages/{messageId}")
    public ApiResponse<String> deleteMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String messageId) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        chatService.deleteMessage(userId, messageId);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Message recalled");
        return response;
    }

    @DeleteMapping("/messages/{messageId}/me")
    public ApiResponse<String> deleteMessageForMe(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String messageId) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        chatService.deleteMessageForMe(userId, messageId);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Message hidden for you");
        return response;
    }

    @PostMapping("/messages/{messageId}/report")
    public ApiResponse<String> reportMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String messageId,
            @RequestBody ReportRequest request) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        chatService.reportMessage(userId, messageId, request.getReason());
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Report submitted");
        return response;
    }

    @PostMapping("/messages/{convId}/typing")
    public ApiResponse<String> typing(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long convId) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        chatService.typing(userId, convId);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Typing event sent");
        return response;
    }
}
