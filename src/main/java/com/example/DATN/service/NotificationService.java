package com.example.DATN.service;

import com.example.DATN.dto.notification.NotificationResponse;
import com.example.DATN.entity.document.Notification;
import com.example.DATN.entity.enums.NotificationType;
import com.example.DATN.entity.enums.TargetType;
import com.example.DATN.exception.AppException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.repository.NotificationRepository;
import com.example.DATN.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class NotificationService {
    NotificationRealTimeService notificationRealTimeService;
    NotificationRepository notificationRepository;
    UserRepository userRepository;

    public void sendNotify(Long recipientId, Long actorId,
                           NotificationType type,
                           String targetId, TargetType targetType) {
        if (recipientId.equals(actorId))
            return;

        String actorName = userRepository.findById(actorId)
                .map(u -> {
                    if (u.getProfile() == null) {
                        return "Người dùng";
                    }

                    if (u.getProfile().getFullName() == null) {
                        return "Người dùng";
                    }

                    return u.getProfile().getFullName();
                })
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String content = switch (type) {
            case LIKE_POST -> actorName + " đã thích bài viết của bạn";
            case COMMENT_POST -> actorName + " đã bình luận bài viết của bạn";
            case REPLY_COMMENT -> actorName + " đã trả lời bình luận của bạn";
            case SHARE_POST -> actorName + " đã chia sẻ bài viết của bạn";
            case NEW_MESSAGE -> actorName + " đã gửi tin nhắn cho bạn";
            case FRIEND_REQUEST -> actorName + " đã gửi lời mời kết bạn";
            case FRIEND_ACCEPTED -> actorName + " đã chấp nhận lời mời kết bạn";
            case GROUP_INVITE -> actorName + " đã mời bạn vào nhóm";
            case JOIN_REQUEST_APPROVED -> actorName + " đã chấp nhận yêu cầu tham gia nhóm của bạn";
            case JOIN_REQUEST_REJECTED -> actorName + " đã từ chối yêu cầu tham gia nhóm của bạn";
            case KICKED_FROM_GROUP -> actorName + " đã xóa bạn khỏi nhóm";
            case JOIN_REQUEST -> actorName + " đã gửi yêu cầu tham gia nhóm";
        };
        Notification notification = Notification.builder()
                .receiverId(recipientId)
                .actorId(actorId)
                .type(type)
                .targetId(targetId)
                .targetType(targetType)
                .content(content)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        notificationRealTimeService.pushToUser(recipientId,toResponse(notification));
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .actorId(n.getActorId())
                .type(n.getType())
                .targetId(n.getTargetId())
                .content(n.getContent())
                .isRead(n.isRead())
                .targetType(n.getTargetType())
                .createAt(n.getCreatedAt())
                .build();
    }


    public Slice<NotificationResponse> getAllMyNotification( Long userId, int page, int size){
        Pageable pageable= PageRequest.of(page,size);
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId,pageable)
                .map(this::toResponse);
    }

    public Long countUnread(Long userId){
        return notificationRepository.countByReceiverIdAndIsReadFalse(userId);
    }

    public void markAllRead(Long userId) {
        List<Notification> unread =
                notificationRepository.findUnreadByRecipientId(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}

