package com.example.DATN.service;

import com.example.DATN.dto.friend.FriendRequest;
import com.example.DATN.dto.friend.FriendResponse;
import com.example.DATN.dto.friend.RecommendUser;
import com.example.DATN.entity.Friendship;
import com.example.DATN.entity.User;
import com.example.DATN.entity.enums.FriendshipStatus;
import com.example.DATN.entity.enums.NotificationType;
import com.example.DATN.entity.enums.TargetType;
import com.example.DATN.exception.AppException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.repository.FriendshipRepository;
import com.example.DATN.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class FriendshipService {

    FriendshipRepository friendshipRepository;
    UserRepository userRepository;
    NotificationService notificationService;

    private String getFullName(User user) {
        return (user.getProfile() != null && user.getProfile().getFullName() != null)
                ? user.getProfile().getFullName()
                : user.getEmail();
    }

    private FriendResponse toFriendResponse(Friendship f, boolean isReceived) {

        User other = isReceived ? f.getUser() : f.getFriend();

        return FriendResponse.builder()
                .friendshipId(f.getId())
                .userId(other.getId())
                .fullName(getFullName(other))
                .since(f.getCreatedAt())
                .build();
    }

    // SEND FRIEND REQUEST

    public void sendFriendRequest(Long senderId, FriendRequest request) {

        if (senderId.equals(request.getTargetUserId())) {
            throw new AppException(ErrorCode.CANNOT_ADD_SELF);
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        User receiver = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // check cả 2 chiều
        List<Friendship> existing =
                friendshipRepository.findFriendshipBetween(
                        senderId,
                        request.getTargetUserId()
                );

        if (!existing.isEmpty()) {

            Friendship friendship = existing.get(0);

            if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new AppException(ErrorCode.ALREADY_FRIEND);
            }

            if (friendship.getStatus() == FriendshipStatus.PENDING) {
                throw new AppException(ErrorCode.FRIEND_REQUEST_ALREADY);
            }
        }

        Friendship friendship = Friendship.builder()
                .user(sender)
                .friend(receiver)
                .status(FriendshipStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        friendshipRepository.save(friendship);

        notificationService.sendNotify(
                receiver.getId(),
                senderId,
                NotificationType.FRIEND_REQUEST,
                friendship.getId().toString(),
                TargetType.FRIEND
        );
    }

    // CANCEL REQUEST

    public void cancelRequestFriend(Long senderId, Long friendshipId) {

        Friendship fr = friendshipRepository.findById(friendshipId)
                .orElseThrow(() ->
                        new AppException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (!fr.getUser().getId().equals(senderId)
                || fr.getStatus() != FriendshipStatus.PENDING) {

            throw new AppException(ErrorCode.NO_PERMISSION_CANCEL_REQUEST);
        }

        friendshipRepository.delete(fr);
    }

    // ACCEPT REQUEST

    public void acceptRequestFriend(Long senderId, Long friendshipId) {

        Friendship fr = friendshipRepository.findById(friendshipId)
                .orElseThrow(() ->
                        new AppException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (!fr.getFriend().getId().equals(senderId)
                || fr.getStatus() != FriendshipStatus.PENDING) {

            throw new AppException(ErrorCode.NO_PERMISSION_ACCEPT_REQUEST);
        }

        fr.setStatus(FriendshipStatus.ACCEPTED);

        friendshipRepository.save(fr);

        notificationService.sendNotify(
                fr.getUser().getId(),
                senderId,
                NotificationType.FRIEND_ACCEPTED,
                friendshipId.toString(),
                TargetType.FRIEND
        );
    }

    // REJECT REQUEST

    public void rejectRequestFriend(Long currentUserId, Long friendshipId) {

        Friendship f = friendshipRepository.findById(friendshipId)
                .orElseThrow(() ->
                        new AppException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (!f.getFriend().getId().equals(currentUserId)
                || f.getStatus() != FriendshipStatus.PENDING) {

            throw new AppException(ErrorCode.NO_PERMISSION_REJECTED_REQUEST);
        }

        f.setStatus(FriendshipStatus.REJECTED);

        friendshipRepository.save(f);
    }

    // UNFRIEND

    public void unfriend(Long senderId, FriendRequest request) {

        List<Friendship> friendships =
                friendshipRepository.findFriendshipBetween(
                        senderId,
                        request.getTargetUserId()
                );

        friendships.forEach(f -> {
            if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                friendshipRepository.delete(f);
            }
        });
    }

    // RECEIVED REQUESTS

    public List<FriendResponse> getReceivedRequests(Long senderId) {

        return friendshipRepository
                .findByFriendIdAndStatus(
                        senderId,
                        FriendshipStatus.PENDING
                )
                .stream()
                .map(f -> toFriendResponse(f, true))
                .collect(Collectors.toList());
    }

    // SENT REQUESTS

    public List<FriendResponse> getSentRequests(Long senderID) {

        return friendshipRepository
                .findByUserIdAndStatus(
                        senderID,
                        FriendshipStatus.PENDING
                )
                .stream()
                .map(f -> toFriendResponse(f, false))
                .collect(Collectors.toList());
    }

    // MY FRIENDS

    public List<FriendResponse> getMyFriends(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow();

        return user.getFriends()
                .stream()
                .map(friend -> {

                    Friendship fr = friendshipRepository
                            .findFriendshipBetween(userId, friend.getId())
                            .stream()
                            .findFirst()
                            .orElseThrow();

                    return FriendResponse.builder()
                            .friendshipId(fr.getId())
                            .userId(friend.getId())
                            .fullName(getFullName(friend))
                            .since(fr.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // MUTUAL FRIENDS

    private int countMutualFriends(User u1, User u2) {

        Set<User> f1 = u1.getFriends();
        Set<User> f2 = u2.getFriends();

        return (int) f1.stream()
                .filter(f2::contains)
                .count();
    }

    // RECOMMENDATIONS

    public List<RecommendUser> getRecommendations(
            Long currentUserId,
            int limit
    ) {

        User current = userRepository.findById(currentUserId)
                .orElseThrow();

        Set<User> myFriends = current.getFriends();

        Set<Long> myFriendIds = myFriends.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        Map<User, Integer> candidates = new HashMap<>();

        for (User friend : myFriends) {

            friend.getFriends().forEach(ff -> {

                if (!ff.getId().equals(currentUserId)
                        && !myFriendIds.contains(ff.getId())
                        && !isPendingWith(currentUserId, ff.getId())) {

                    int mutual = countMutualFriends(current, ff);

                    candidates.merge(ff, mutual, Math::max);
                }
            });
        }

        if (candidates.size() < limit) {

            userRepository.findAll()
                    .stream()
                    .filter(u ->
                            u.isTeacher()
                                    && !u.getId().equals(currentUserId)
                                    && !myFriendIds.contains(u.getId())
                                    && !isPendingWith(currentUserId, u.getId())
                    )
                    .limit(20)
                    .forEach(u -> candidates.putIfAbsent(u, 0));
        }

        return candidates.entrySet()
                .stream()
                .sorted((a, b) ->
                        b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(entry -> {

                    User u = entry.getKey();
                    int mutual = entry.getValue();

                    String reason =
                            mutual > 0
                                    ? "Có " + mutual + " bạn chung"
                                    : u.isTeacher()
                                    ? "Giáo viên được xác thực"
                                    : "Người dùng mới";

                    return RecommendUser.builder()
                            .userId(u.getId())
                            .email(u.getEmail())
                            .mutualFriendsCount((long) mutual)
                            .isTeacher(u.isTeacher())
                            .createAt(u.getCreateAt())
                            .reason(reason)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // CHECK PENDING
    private boolean isPendingWith(Long currentId, Long otherId) {

        return friendshipRepository
                .findByUserIdAndFriendId(currentId, otherId)
                .map(f -> f.getStatus() == FriendshipStatus.PENDING)
                .orElse(false)

                ||

                friendshipRepository
                        .findByUserIdAndFriendId(otherId, currentId)
                        .map(f -> f.getStatus() == FriendshipStatus.PENDING)
                        .orElse(false);
    }
}