package com.example.DATN.controller;

import com.example.DATN.dto.ApiResponse;
import com.example.DATN.dto.chat.ConversationResponse;
import com.example.DATN.dto.chat.CreateChatGroup;
import com.example.DATN.dto.chat.ReportRequest;
import com.example.DATN.service.ConversationService;
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
@RequestMapping("/conversations")
public class ConversationController {

    ConversationService conversationService;

    @PostMapping("/one-to-one/{targetUserId}")
    public ApiResponse<ConversationResponse> createOrGetOneToOne(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long targetUserId) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        ApiResponse<ConversationResponse> response = new ApiResponse<>();
        response.setResult(conversationService.createOrGetOneToOne(userId, targetUserId));
        return response;
    }

    @PostMapping("/group")
    public ApiResponse<ConversationResponse> createGroup(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreateChatGroup request) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        ApiResponse<ConversationResponse> response = new ApiResponse<>();
        response.setResult(conversationService.createGroupChat(userId, request));
        return response;
    }

    @PostMapping("/{convId}/members")
    public ApiResponse<String> addMembers(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long convId,
            @RequestBody List<Long> memberIds) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        conversationService.addParticipant(convId, userId, memberIds);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Members added");
        return response;
    }

    @DeleteMapping("/{convId}/members")
    public ApiResponse<String> removeMembers(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long convId,
            @RequestBody List<Long> memberIds) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        conversationService.removeMember(convId, userId, memberIds);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Members removed");
        return response;
    }

    @DeleteMapping("/{convId}/leave")
    public ApiResponse<String> leaveGroup(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long convId) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        conversationService.leaveGroup(convId, userId);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Left group");
        return response;
    }

    @PutMapping("/{convId}/owner/{targetUserId}")
    public ApiResponse<String> promoteOwner(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long convId,
            @PathVariable Long targetUserId) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        conversationService.promoteToOwner(convId, userId, targetUserId);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Owner transferred");
        return response;
    }

    @PutMapping("/{convId}/accept")
    public ApiResponse<String> acceptConversation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long convId) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        conversationService.acceptConversation(userId, convId);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Conversation accepted");
        return response;
    }

    @PutMapping("/{convId}/reject")
    public ApiResponse<String> rejectConversation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long convId) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        conversationService.rejectConversation(userId, convId);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Conversation rejected");
        return response;
    }

    @DeleteMapping("/{convId}")
    public ApiResponse<String> deleteConversation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long convId) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        conversationService.deleteConversation(userId, convId);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Conversation deleted");
        return response;
    }

    @PutMapping("/{convId}/archive")
    public ApiResponse<String> archive(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long convId) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        conversationService.archiveConversation(userId, convId);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Conversation archived");
        return response;
    }

    @PutMapping("/{convId}/unarchive")
    public ApiResponse<String> unarchive(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long convId) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        conversationService.unArchiveConversation(userId, convId);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Conversation unarchived");
        return response;
    }

    @GetMapping("/archived")
    public ApiResponse<List<ConversationResponse>> getArchived(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        ApiResponse<List<ConversationResponse>> response = new ApiResponse<>();
        response.setResult(conversationService.getConversationArchive(userId));
        return response;
    }

    @PostMapping("/report")
    public ApiResponse<String> reportConversation(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ReportRequest request) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        conversationService.reportConversation(userId, request);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Report submitted");
        return response;
    }
}
