package com.example.DATN.controller;

import com.example.DATN.dto.ApiResponse;
import com.example.DATN.dto.group.*;
import com.example.DATN.dto.post.PostResponse;
import com.example.DATN.entity.enums.GroupPrivacy;
import com.example.DATN.entity.enums.MembershipRole;
import com.example.DATN.service.GroupService;
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

import java.util.List;

@RestController
@RequestMapping("/groups")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class GroupController {

    GroupService groupService;

    // 1. Tạo nhóm mới (chỉ giáo viên)
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse createGroup(
            @RequestBody @Valid GroupCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        GroupResponse group = groupService.createGroup(userId, request);

        ApiResponse response = new ApiResponse();
        response.setResult(group);
        response.setMessage("Tạo nhóm thành công");
        return response;
    }

    // 2. Gửi yêu cầu tham gia nhóm
    @PostMapping("/{groupId}/join-request")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse requestToJoinGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        groupService.requestToJoinGroup(userId, groupId);

        ApiResponse response = new ApiResponse();
        response.setMessage("Yêu cầu tham gia nhóm đã được gửi");
        return response;
    }

    // 3. Hủy yêu cầu tham gia (người gửi hủy)
    @DeleteMapping("/{groupId}/join-request")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse cancelJoinRequest(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        groupService.cancelJoinRequest(userId, groupId);

        ApiResponse response = new ApiResponse();
        response.setMessage("Đã hủy yêu cầu tham gia nhóm");
        return response;
    }

    // 4. Duyệt yêu cầu tham gia (chỉ owner)
    @PostMapping("/requests/{requestId}/approve")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse approveJoinRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal Jwt jwt) {

        Long approverId = ((Number) jwt.getClaim("userId")).longValue();

        groupService.approveJoinRequest(approverId, requestId);

        ApiResponse response = new ApiResponse();
        response.setMessage("Đã duyệt yêu cầu tham gia");
        return response;
    }

    // 5. Từ chối yêu cầu tham gia (chỉ owner)
    @PostMapping("/requests/{requestId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse rejectJoinRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal Jwt jwt) {

        Long approverId = ((Number) jwt.getClaim("userId")).longValue();

        groupService.rejectJoinRequest(approverId, requestId);

        ApiResponse response = new ApiResponse();
        response.setMessage("Đã từ chối yêu cầu tham gia");
        return response;
    }

    // 6. Mời bạn bè tham gia nhóm
    @PostMapping("/{groupId}/invite/{friendId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse inviteFriend(
            @PathVariable Long groupId,
            @PathVariable Long friendId,
            @AuthenticationPrincipal Jwt jwt) {

        Long inviterId = ((Number) jwt.getClaim("userId")).longValue();

        groupService.inviteFriend(inviterId, groupId, friendId);

        ApiResponse response = new ApiResponse();
        response.setMessage("Đã gửi lời mời tham gia nhóm");
        return response;
    }

    // 7. Chấp nhận lời mời (người được mời)
    @PostMapping("/invitations/{requestId}/accept")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse acceptInvitation(
            @PathVariable Long requestId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        groupService.acceptInvitation(userId, requestId);

        ApiResponse response = new ApiResponse();
        response.setMessage("Đã tham gia nhóm thành công");
        return response;
    }

    // 8. Từ chối lời mời (người được mời)
    @PostMapping("/invitations/{requestId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse rejectInvitation(
            @PathVariable Long requestId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        groupService.rejectInvitation(userId, requestId);

        ApiResponse response = new ApiResponse();
        response.setMessage("Đã từ chối lời mời tham gia nhóm");
        return response;
    }

    // 9. Rời nhóm
    @DeleteMapping("/{groupId}/leave")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse leaveGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        groupService.leaveGroup(userId, groupId);

        ApiResponse response = new ApiResponse();
        response.setMessage("Đã rời nhóm thành công");
        return response;
    }

    // 10. Kick thành viên (chỉ owner)
    @DeleteMapping("/{groupId}/members/{memberId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse removeMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal Jwt jwt) {

        Long approverId = ((Number) jwt.getClaim("userId")).longValue();

        groupService.removeMember(approverId, groupId, memberId);

        ApiResponse response = new ApiResponse();
        response.setMessage("Đã xóa thành viên khỏi nhóm");
        return response;
    }

    // 11. Thay đổi vai trò thành viên (chỉ owner)
    @PutMapping("/{groupId}/members/{targetUserId}/role")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse changeRole(
            @PathVariable Long groupId,
            @PathVariable Long targetUserId,
            @RequestBody @Valid ChangeRoleRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long approverId = ((Number) jwt.getClaim("userId")).longValue();

        groupService.changeRole(approverId, groupId, targetUserId, request.getNewRole());

        ApiResponse response = new ApiResponse();
        response.setMessage("Đã thay đổi vai trò thành viên");
        return response;
    }

    // 12. Chuyển quyền sở hữu nhóm
    @PutMapping("/{groupId}/transfer-ownership/{newOwnerId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse transferOwnership(
            @PathVariable Long groupId,
            @PathVariable Long newOwnerId,
            @AuthenticationPrincipal Jwt jwt) {

        Long currentOwnerId = ((Number) jwt.getClaim("userId")).longValue();

        groupService.transferOwnership(currentOwnerId, groupId, newOwnerId);

        ApiResponse response = new ApiResponse();
        response.setMessage("Đã chuyển quyền sở hữu nhóm thành công");
        return response;
    }

    // 13. Lấy danh sách yêu cầu đang chờ duyệt (chỉ owner)
    @GetMapping("/{groupId}/pending-requests")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse getPendingJoinRequests(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        List<JoinRequestResponse> requests = groupService.getPendingJoinRequests(userId, groupId);

        ApiResponse response = new ApiResponse();
        response.setResult(requests);
        return response;
    }

    // 14. Lấy danh sách thành viên nhóm
    @GetMapping("/{groupId}/members")
    public ApiResponse getGroupMembers(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Jwt jwt) {

        Long viewerId = ((Number) jwt.getClaim("userId")).longValue();

        List<GroupMemberResponse> members = groupService.getGroupMembers(groupId, viewerId);

        ApiResponse response = new ApiResponse();
        response.setResult(members);
        return response;
    }

    // 15. Lấy danh sách nhóm của tôi
    @GetMapping("/my-groups")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse getMyGroups(@AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        List<GroupResponse> groups = groupService.getMyGroups(userId);

        ApiResponse response = new ApiResponse();
        response.setResult(groups);
        return response;
    }

    // 16. Lấy chi tiết nhóm
    @GetMapping("/{groupId}")
    public ApiResponse getGroupDetail(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Jwt jwt) {

        Long viewerId = ((Number) jwt.getClaim("userId")).longValue();

        GroupResponse group = groupService.getGroupDetail(groupId, viewerId);

        ApiResponse response = new ApiResponse();
        response.setResult(group);
        return response;
    }

    // 17. Cập nhật thông tin nhóm (chỉ owner)
    @PutMapping("/{groupId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse updateGroupInfo(
            @PathVariable Long groupId,
            @RequestBody @Valid GroupUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        GroupResponse updated = groupService.updateGroupInfo(userId, groupId, request);

        ApiResponse response = new ApiResponse();
        response.setResult(updated);
        response.setMessage("Cập nhật thông tin nhóm thành công");
        return response;
    }

    // 18. Xóa nhóm (chỉ owner)
    @DeleteMapping("/{groupId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse deleteGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        groupService.deleteGroup(userId, groupId);

        ApiResponse response = new ApiResponse();
        response.setMessage("Đã xóa nhóm thành công");
        return response;
    }

    // 19. Ghim bài viết trong nhóm (owner/mod)
    @PostMapping("/{groupId}/posts/{postId}/pin")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse pinPost(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        groupService.pinPost(userId, groupId, postId);

        ApiResponse response = new ApiResponse();
        response.setMessage("Đã ghim bài viết");
        return response;
    }

    // 20. Bỏ ghim bài viết
    @DeleteMapping("/{groupId}/posts/{postId}/pin")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse unpinPost(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        groupService.unpinPost(userId, groupId, postId);

        ApiResponse response = new ApiResponse();
        response.setMessage("Đã bỏ ghim bài viết");
        return response;
    }

    // 21. Lấy feed của nhóm (bài viết trong nhóm)
    @GetMapping("/{groupId}/feed")
    public ApiResponse getGroupFeed(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) {

        Long viewerId = ((Number) jwt.getClaim("userId")).longValue();

        Slice<PostResponse> feed = groupService.getGroupFeed(groupId, viewerId, page, size);

        ApiResponse response = new ApiResponse();
        response.setResult(feed);
        return response;
    }

    // 22. Tìm kiếm nhóm theo từ khóa
    @GetMapping("/search")
    public ApiResponse searchGroups(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {

        List<GroupResponse> groups = groupService.searchGroups(keyword);

        ApiResponse response = new ApiResponse();
        response.setResult(groups.subList(0, Math.min(groups.size(), limit)));
        return response;
    }

    // 23. Gợi ý nhóm (có thể mở rộng sau)
    @GetMapping("/suggest")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse suggestGroups(@AuthenticationPrincipal Jwt jwt) {

        Long userId = ((Number) jwt.getClaim("userId")).longValue();

        List<GroupResponse> suggestions = groupService.suggestGroups(userId);

        ApiResponse response = new ApiResponse();
        response.setResult(suggestions);
        return response;
    }

}