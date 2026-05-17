package com.example.DATN.service;

import com.example.DATN.dto.group.*;
import com.example.DATN.dto.post.PostResponse;
import com.example.DATN.entity.*;
import com.example.DATN.entity.enums.*;
import com.example.DATN.exception.AppException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class GroupService {

    GroupRepository groupRepository;
    GroupMembershipRepository membershipRepository;
    GroupJoinRequestRepository joinRequestRepository;
    UserRepository userRepository;
    FriendshipRepository friendshipRepository;
    PostRepository postRepository;
    PostService postService;
    NotificationService notificationService;

    //  CREATE GROUP
    public GroupResponse createGroup(Long userId, GroupCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.isTeacher()) {
            throw new AppException(ErrorCode.NO_PERMISSION_CREATE_GROUP);
        }

        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .coverImageUrl(request.getCoverImageUrl())
                .privacy(request.getPrivacy() != null ? request.getPrivacy() : GroupPrivacy.PRIVATE)
                .owner(user)
                .build();

        group = groupRepository.save(group);

        membershipRepository.save(GroupMembership.builder()
                .group(group)
                .user(user)
                .role(MembershipRole.OWNER)
                .build());

        return mapToGroupResponse(group);
    }

    //  JOIN REQUEST
    public void requestToJoinGroup(Long userId, Long groupId) {
        User user = userRepository.findById(userId).orElseThrow();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_EXISTED));

        if (membershipRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new AppException(ErrorCode.ALREADY_IN_GROUP);
        }

        if (joinRequestRepository.findByGroupIdAndUserId(groupId, userId).isPresent()) {
            throw new AppException(ErrorCode.JOIN_REQUEST_ALREADY_SENT);
        }

        GroupJoinRequest req = GroupJoinRequest.builder()
                .group(group)
                .user(user)
                .build();
        joinRequestRepository.save(req);
        notificationService.sendNotify(
                group.getOwner().getId(),
                userId,
                NotificationType.JOIN_REQUEST,
                groupId.toString(),
                TargetType.GROUP
        );
    }

    // huy join request
    public void cancelJoinRequest(Long userId, Long groupId) {
        GroupJoinRequest req = joinRequestRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.JOIN_REQUEST_NOT_FOUND));

        if (req.getStatus() != JoinRequestStatus.PENDING) {
            throw new AppException(ErrorCode.REQUEST_ALREADY_PROCESSED);
        }

        joinRequestRepository.delete(req);
    }

    //accept join request
    public void approveJoinRequest(Long approverId, Long requestId) {
        GroupJoinRequest req = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.JOIN_REQUEST_NOT_FOUND));

        if (!req.getGroup().getOwner().getId().equals(approverId)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        if (req.getStatus() != JoinRequestStatus.PENDING) {
            throw new AppException(ErrorCode.REQUEST_ALREADY_PROCESSED);
        }

        req.setStatus(JoinRequestStatus.APPROVED);
        req.setReviewedAt(LocalDateTime.now());
        joinRequestRepository.save(req);

        membershipRepository.save(GroupMembership.builder()
                .group(req.getGroup())
                .user(req.getUser())
                .role(MembershipRole.MEMBER)
                .build());
        notificationService.sendNotify(
                req.getUser().getId(),
                approverId,
                NotificationType.JOIN_REQUEST_APPROVED,
                req.getGroup().getId().toString(),
                TargetType.GROUP
        );
    }

    //reject join request
    public void rejectJoinRequest(Long approverId, Long requestId) {
        GroupJoinRequest req = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.JOIN_REQUEST_NOT_FOUND));

        if (!req.getGroup().getOwner().getId().equals(approverId)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        if (req.getStatus() != JoinRequestStatus.PENDING) {
            throw new AppException(ErrorCode.REQUEST_ALREADY_PROCESSED);
        }

        req.setStatus(JoinRequestStatus.REJECTED);
        req.setReviewedAt(LocalDateTime.now());
        joinRequestRepository.save(req);
        notificationService.sendNotify(
                req.getUser().getId(),
                approverId,
                NotificationType.JOIN_REQUEST_REJECTED,
                req.getGroup().getId().toString(),
                TargetType.GROUP
        );
    }

    //  mời bạn bè
    public void inviteFriend(Long inviterId, Long groupId, Long friendId) {
        if (!membershipRepository.existsByGroupIdAndUserId(groupId, inviterId)) {
            throw new AppException(ErrorCode.NOT_IN_GROUP);
        }

        if (joinRequestRepository.findByGroupIdAndUserId(groupId, friendId).isPresent()) {
            throw new AppException(ErrorCode.JOIN_REQUEST_ALREADY_SENT);
        }

        Friendship friendship = friendshipRepository.findFriendshipBetween(inviterId, friendId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FRIENDS));

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new AppException(ErrorCode.CAN_ONLY_INVITE_FRIENDS);
        }

        Group group = groupRepository.findById(groupId).orElseThrow();
        User friend = userRepository.findById(friendId).orElseThrow();

        if (membershipRepository.existsByGroupIdAndUserId(groupId, friendId)) {
            throw new AppException(ErrorCode.ALREADY_IN_GROUP);
        }

        GroupJoinRequest req = GroupJoinRequest.builder()
                .group(group)
                .user(friend)
                .inviter(userRepository.findById(inviterId).orElseThrow())
                .build();
        joinRequestRepository.save(req);
        notificationService.sendNotify(
                friendId,
                inviterId,
                NotificationType.GROUP_INVITE,
                groupId.toString(),
                TargetType.GROUP
        );
    }

    // accept lời mời
    public void acceptInvitation(Long userId, Long requestId) {
        GroupJoinRequest req = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.JOIN_REQUEST_NOT_FOUND));

        if (!req.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        if (req.getStatus() != JoinRequestStatus.PENDING) {
            throw new AppException(ErrorCode.REQUEST_ALREADY_PROCESSED);
        }

        req.setStatus(JoinRequestStatus.APPROVED);
        req.setReviewedAt(LocalDateTime.now());
        joinRequestRepository.save(req);

        membershipRepository.save(GroupMembership.builder()
                .group(req.getGroup())
                .user(req.getUser())
                .role(MembershipRole.MEMBER)
                .build());
    }

    // từ chối lời mời
    public void rejectInvitation(Long userId, Long requestId) {
        GroupJoinRequest req = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.JOIN_REQUEST_NOT_FOUND));

        if (!req.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        if (req.getStatus() != JoinRequestStatus.PENDING) {
            throw new AppException(ErrorCode.REQUEST_ALREADY_PROCESSED);
        }

        req.setStatus(JoinRequestStatus.REJECTED);
        req.setReviewedAt(LocalDateTime.now());
        joinRequestRepository.save(req);
    }

    //  out group
    public void leaveGroup(Long userId, Long groupId) {
        GroupMembership membership = membershipRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_IN_GROUP));

        if (membership.getRole() == MembershipRole.OWNER) {
            boolean hasOtherMod = membershipRepository.findByGroupId(groupId).stream()
                    .anyMatch(m -> m.getRole() == MembershipRole.MODERATOR && !m.getUser().getId().equals(userId));
            if (!hasOtherMod) {
                throw new AppException(ErrorCode.NO_PERMISSION);
            }
        }

        membershipRepository.delete(membership);
    }

    //kick member
    public void removeMember(Long approverId, Long groupId, Long memberId) {
        Group group = groupRepository.findById(groupId).orElseThrow();

        if (!group.getOwner().getId().equals(approverId)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        if (memberId.equals(approverId)) {
            throw new AppException(ErrorCode.CANNOT_REMOVE_SELF);
        }

        GroupMembership membership = membershipRepository.findByGroupIdAndUserId(groupId, memberId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_IN_GROUP));

        membershipRepository.delete(membership);
        notificationService.sendNotify(
                memberId,
                approverId,
                NotificationType.KICKED_FROM_GROUP,
                groupId.toString(),
                TargetType.GROUP
        );
    }

    // change role group
    public void changeRole(Long approverId, Long groupId, Long targetUserId, MembershipRole newRole) {
        Group group = groupRepository.findById(groupId).orElseThrow();

        if (!group.getOwner().getId().equals(approverId)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        if (newRole == MembershipRole.OWNER) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        GroupMembership membership = membershipRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_IN_GROUP));

        membership.setRole(newRole);
        membershipRepository.save(membership);
    }


    public void transferOwnership(Long currentOwnerId, Long groupId, Long newOwnerId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_EXISTED));

        if (!group.getOwner().getId().equals(currentOwnerId)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        GroupMembership currentOwner = membershipRepository.findByGroupIdAndUserId(groupId, currentOwnerId)
                .orElseThrow();
        GroupMembership newOwner = membershipRepository.findByGroupIdAndUserId(groupId, newOwnerId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_IN_GROUP));

        currentOwner.setRole(MembershipRole.MEMBER);
        newOwner.setRole(MembershipRole.OWNER);
        group.setOwner(newOwner.getUser());

        membershipRepository.save(currentOwner);
        membershipRepository.save(newOwner);
        groupRepository.save(group);
    }

    //  GROUP INFO & VISIBILITY
    public List<JoinRequestResponse> getPendingJoinRequests(Long userId, Long groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow();

        if (!group.getOwner().getId().equals(userId)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        return joinRequestRepository.findByGroupIdAndStatus(groupId, JoinRequestStatus.PENDING)
                .stream()
                .map(this::mapToJoinRequestResponse)
                .collect(Collectors.toList());
    }

    public List<GroupMemberResponse> getGroupMembers(Long groupId, Long viewerId) {
        Group group = groupRepository.findById(groupId).orElseThrow();

        boolean isMember = membershipRepository.existsByGroupIdAndUserId(groupId, viewerId);
        if (group.getPrivacy() != GroupPrivacy.PUBLIC && !isMember) {
            throw new AppException(ErrorCode.NO_PERMISSION_TO_VIEW_GROUP);
        }

        return membershipRepository.findByGroupId(groupId)
                .stream()
                .map(m -> GroupMemberResponse.builder()
                        .userId(m.getUser().getId())
                        .fullName(m.getUser().getProfile() != null ?
                                m.getUser().getProfile().getFullName() : m.getUser().getEmail())
                        .role(m.getRole())
                        .joinedAt(m.getJoinedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public List<GroupResponse> getMyGroups(Long userId) {
        return membershipRepository.findByUserId(userId)
                .stream()
                .map(m -> mapToGroupResponse(m.getGroup()))
                .collect(Collectors.toList());
    }

    public GroupResponse getGroupDetail(Long groupId, Long viewerId) {
        Group group = groupRepository.findById(groupId).orElseThrow();

        boolean isMember = membershipRepository.existsByGroupIdAndUserId(groupId, viewerId);
        if (group.getPrivacy() != GroupPrivacy.PUBLIC && !isMember) {
            throw new AppException(ErrorCode.NO_PERMISSION_TO_VIEW_GROUP);
        }

        return mapToGroupResponse(group);
    }

    public GroupResponse updateGroupInfo(Long userId, Long groupId, GroupUpdateRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_EXISTED));

        if (!group.getOwner().getId().equals(userId)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        if (request.getName() != null) group.setName(request.getName());
        if (request.getDescription() != null) group.setDescription(request.getDescription());
        if (request.getCoverImageUrl() != null) group.setCoverImageUrl(request.getCoverImageUrl());
        if (request.getPrivacy() != null) group.setPrivacy(request.getPrivacy());

        group = groupRepository.save(group);
        return mapToGroupResponse(group);
    }

    public void deleteGroup(Long userId, Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_EXISTED));

        if (!group.getOwner().getId().equals(userId)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        membershipRepository.deleteByGroupId(groupId);
        joinRequestRepository.deleteByGroupId(groupId);

        List<Post> posts = postRepository.findByGroupId(groupId);
        for (Post post : posts) {
            postService.deletePost(post.getUserId(), post.getId());
        }

        groupRepository.delete(group);
    }

    //  PIN/UNPIN POST
    public void pinPost(Long userId, Long groupId, Long postId) {
        togglePinPost(userId, groupId, postId, true);
    }

    public void unpinPost(Long userId, Long groupId, Long postId) {
        togglePinPost(userId, groupId, postId, false);
    }

    private void togglePinPost(Long userId, Long groupId, Long postId, boolean pin) {
        Group group = groupRepository.findById(groupId).orElseThrow();
        Post post = postRepository.findById(postId).orElseThrow();

        if (!post.getGroup().getId().equals(groupId)) {
            throw new AppException(ErrorCode.POST_NOT_IN_GROUP);
        }

        GroupMembership membership = membershipRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_IN_GROUP));

        if (membership.getRole() != MembershipRole.OWNER && membership.getRole() != MembershipRole.MODERATOR) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        post.setPinned(pin);
        postRepository.save(post);
    }

    //  FEED
    public Slice<PostResponse> getGroupFeed(Long groupId, Long viewerId, int page, int size) {
        Group group = groupRepository.findById(groupId).orElseThrow();

        boolean isMember = membershipRepository.existsByGroupIdAndUserId(groupId, viewerId);
        if (group.getPrivacy() != GroupPrivacy.PUBLIC && !isMember) {
            throw new AppException(ErrorCode.NO_PERMISSION_TO_VIEW_GROUP);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Slice<Post> posts = postRepository.findByGroupIdAndIsHiddenFalse(groupId, pageable);

        return posts.map(postService::mapToResponse);
    }

    //  DISCOVERY
    public List<GroupResponse> searchGroups(String keyword) {
        return groupRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::mapToGroupResponse)
                .collect(Collectors.toList());
    }

    public List<GroupResponse> suggestGroups(Long userId) {
        List<Long> joinedGroupIds = membershipRepository.findByUserId(userId)
                .stream()
                .map(m -> m.getGroup().getId())
                .toList();

        return groupRepository.findAll().stream()
                .filter(g -> !joinedGroupIds.contains(g.getId()))
                .limit(10)
                .map(this::mapToGroupResponse)
                .collect(Collectors.toList());
    }

    //  MAPPING HELPERS
    private GroupResponse mapToGroupResponse(Group group) {
        String ownerName = group.getOwner().getProfile() != null ?
                group.getOwner().getProfile().getFullName() : group.getOwner().getEmail();

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .coverImageUrl(group.getCoverImageUrl())
                .privacy(group.getPrivacy())
                .ownerId(group.getOwner().getId())
                .ownerName(ownerName)
                .memberCount(membershipRepository.countByGroupId(group.getId()))
                .createdAt(group.getCreatedAt())
                .build();
    }

    private JoinRequestResponse mapToJoinRequestResponse(GroupJoinRequest req) {
        String userName = req.getUser().getProfile() != null ?
                req.getUser().getProfile().getFullName() : req.getUser().getEmail();

        String inviterName = req.getInviter() != null ?
                (req.getInviter().getProfile() != null ?
                        req.getInviter().getProfile().getFullName() : req.getInviter().getEmail()) : null;

        return JoinRequestResponse.builder()
                .id(req.getId())
                .userId(req.getUser().getId())
                .userName(userName)
                .inviterId(req.getInviter() != null ? req.getInviter().getId() : null)
                .inviterName(inviterName)
                .status(req.getStatus())
                .requestedAt(req.getRequestedAt())
                .build();
    }
}