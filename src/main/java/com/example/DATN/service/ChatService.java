package com.example.DATN.service;

import com.example.DATN.dto.Medias;
import com.example.DATN.dto.chat.*;
import com.example.DATN.entity.Conversation;
import com.example.DATN.entity.document.MessageMedia;
import com.example.DATN.entity.document.MessageReaction;
import com.example.DATN.entity.document.Messages;
import com.example.DATN.entity.document.Report;
import com.example.DATN.entity.enums.MessageStatus;
import com.example.DATN.entity.enums.TypeEvent;
import com.example.DATN.exception.AppException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.repository.*;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ChatService {

    MessageRepository messageRepository;
    ConversationService conversationService;
    ParticipantRepository participantRepository;
    BlockRepository blockRepository;
    UploadService uploadService;
    UserRepository userRepository;
    FriendshipRepository friendshipRepository;
    BlockService blockService;
    MessageReactionRepository messageReactionRepository;
    ChatRealTimeService chatRealTimeService;
    ReportChatRepository reportChatRepository;

    private MessageResponse toMessageResponse(Messages messages) {
        return MessageResponse.builder()
                .id(messages.getId())
                .conversationId(messages.getConversationId())
                .senderId(messages.getSenderId())
                .content(messages.getContent())
                .messageMedias(messages.getMediaList())
                .timestamp(messages.getTimestamp())
                .status(messages.getStatus())
                .seenBy(messages.getSeenBy())
                .isPending(messages.isPending())
                .isEdited(messages.isEdited())
                .build();
    }
    private <T> EventMessages<T> buildEvent(TypeEvent type, Long convId, Long userId,
                                            String messageId, T payload) {
        return EventMessages.<T>builder()
                .type(type)
                .conversationId(convId)
                .userId(userId)
                .messageId(messageId)
                .payload(payload)
                .build();
    }

    //  send
    public MessageResponse sendMessages(Long senderId, SendMessageRequest request,
                                        List<MultipartFile> files) {
        Conversation conv = conversationService.getConversationById(
                request.getConversationId(), senderId);
        conversationService.assertParticipant(senderId, conv);

        // Block check for 1-1
        if (!conv.isGroup()) {
            Long receiverId = conv.getParticipants().stream()
                    .filter(p -> !p.getUser().getId().equals(senderId))
                    .map(p -> p.getUser().getId())
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            if (blockService.isBlocked(senderId, receiverId)) {
                throw new AppException(ErrorCode.BLOCKED_USER);
            }
        }

        Messages messages = Messages.builder()
                .conversationId(request.getConversationId())
                .senderId(senderId)
                .content(request.getContent())
                .timestamp(Instant.now())
                .status(MessageStatus.SENT)
                .isEdited(false)
                .isPending(true)
                .build();
        messageRepository.save(messages);

        // Upload media nếu có
        if (files != null && !files.isEmpty()) {
            List<Medias> medias = uploadService.uploadMedias(files, "messages", messages.getId());
            messages.setMediaList(toMessageMedia(medias));
            messageRepository.save(messages);
        }

        conversationService.updateLastMessage(conv, messages);

        MessageResponse response = toMessageResponse(messages);

        //  REALTIME: push tới mọi người trong conversation 
        EventMessages<MessageResponse> event = buildEvent(
                TypeEvent.MESSAGE,
                request.getConversationId(),
                senderId,
                messages.getId(),
                response);

        chatRealTimeService.sendToConversation(request.getConversationId(), event);

        // Push thông báo cho người offline
        chatRealTimeService.pushToOffline(request.getConversationId(), senderId, messages);

        return response;
    }

    //  get messages (paged)

    public Slice<MessageResponse> getMessages(Long convId, Long userId, int page, int size) {

        conversationService.getConversationById(convId, userId); // assert participant inside

        var pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        var slice = messageRepository.findByConversationIdOrderByTimestampDesc(convId, pageable);
        Set<Long> blockedUserIds = new HashSet<>(blockService.getAllHiddenUserIds(userId));

        List<MessageResponse> responses = slice.getContent().stream()
                .filter(msg -> !msg.getDeletedFor().contains(userId))
                .map(msg -> {
                    boolean isBlocked = blockedUserIds.contains(msg.getSenderId());
                    return MessageResponse.builder()
                            .id(msg.getId())
                            .conversationId(msg.getConversationId())
                            .senderId(msg.getSenderId())
                            .content(isBlocked ? "Tin nhắn bị ẩn" : msg.getContent())
                            .messageMedias(isBlocked ? null : msg.getMediaList())
                            .timestamp(msg.getTimestamp())
                            .status(msg.getStatus())
                            .seenBy(msg.getSeenBy())
                            .isEdited(msg.isEdited())
                            .isPending(msg.isPending())
                            .build();
                })
                .toList();

        return new SliceImpl<>(responses, pageable, slice.hasNext());
    }

    //  mark seen
    public void markMessages(Long convId, Long userId) {
        Conversation conv = conversationService.getConversationById(convId, userId);
        conversationService.assertParticipant(userId, conv);

        Set<Long> hiddenUsers = blockService.getAllHiddenUserIds(userId);

        List<Messages> toUpdate = messageRepository
                .findByConversationIdAndIsPendingTrue(convId)
                .stream()
                .filter(msg -> !msg.getSenderId().equals(userId))
                .filter(msg -> !hiddenUsers.contains(msg.getSenderId()))
                .filter(msg -> !msg.getSeenBy().contains(userId))
                .toList();

        if (toUpdate.isEmpty()) return;
        
        toUpdate.forEach(msg -> {
            msg.getSeenBy().add(userId);
            msg.setPending(false);
        });
        messageRepository.saveAll(toUpdate);

        EventMessages<Map<String, Object>> seenEvent = buildEvent(
                TypeEvent.SEEN,
                convId,
                userId,
                null,
                Map.of("userId", userId, "conversationId", convId));

        chatRealTimeService.sendToConversation(convId, seenEvent);
    }

    //  reaction
    public void reactionMessage(Long userId, ReactionMessageRequest request) {
        Messages messages = messageRepository.findById(request.getMessageId())
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        messageReactionRepository.findByMessageIdAndUserId(messages.getId(), userId)
                .ifPresentOrElse(
                        r -> {
                            r.setType(request.getType());
                            messageReactionRepository.save(r);
                        },
                        () -> {
                            MessageReaction reaction = MessageReaction.builder()
                                    .messageId(request.getMessageId())
                                    .type(request.getType())
                                    .userId(userId)
                                    .build();
                            messageReactionRepository.save(reaction);
                        });

        //  REALTIME: broadcast reaction tới conversation
        EventMessages<Map<String, Object>> event = buildEvent(
                TypeEvent.REACTION,
                messages.getConversationId(),
                userId,
                messages.getId(),
                Map.of(
                        "messageId", messages.getId(),
                        "userId", userId,
                        "reaction", request.getType()));
        chatRealTimeService.sendToConversation(messages.getConversationId(), event);
    }

    //  reply
    public MessageResponse replyMessage(Long userId, RepplyMessage request) {
        Messages parent = messageRepository.findById(request.getParentMessageId())
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        Messages reply = Messages.builder()
                .conversationId(request.getSendRequest().getConversationId())
                .senderId(userId)
                .content(request.getSendRequest().getContent())
                .replyToMessageId(request.getParentMessageId())
                .timestamp(Instant.now())
                .status(MessageStatus.SENT)
                .isPending(true)
                .isEdited(false)
                .build();
        messageRepository.save(reply);

        conversationService.updateLastMessage(
                conversationService.getConversationById(reply.getConversationId(), userId),
                reply);

        MessageResponse response = toMessageResponse(reply);

        EventMessages<MessageResponse> event = buildEvent(
                TypeEvent.MESSAGE,
                reply.getConversationId(),
                userId,
                reply.getId(),
                response);

        chatRealTimeService.sendToConversation(reply.getConversationId(), event);
        chatRealTimeService.pushToOffline(reply.getConversationId(), userId, reply);

        return response;
    }

    //  typing
    public void typing(Long userId, Long convId) {
        chatRealTimeService.sendToConversation(
                convId,
                buildEvent(TypeEvent.TYPING, convId, userId, null, null));
    }

    //  edit
    public MessageResponse editMessage(Long userId, String messageId, String newContent) {
        Messages msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!msg.getSenderId().equals(userId)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        msg.setContent(newContent);
        msg.setEdited(true);
        messageRepository.save(msg);

        MessageResponse response = toMessageResponse(msg);

        EventMessages<MessageResponse> event = buildEvent(
                TypeEvent.EDIT,
                msg.getConversationId(),
                userId,
                messageId,
                response);

        chatRealTimeService.sendToConversation(msg.getConversationId(), event);

        return response;
    }

    //  delete
    public void deleteMessage(Long userId, String messageId) {
        Messages msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!msg.getSenderId().equals(userId)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        // Soft delete: xóa với MỌI NGƯỜI (thu hồi)
        msg.setContent("Tin nhắn đã bị thu hồi");
        msg.setMediaList(null);
        msg.setEdited(true);
        messageRepository.save(msg);

        EventMessages<Map<String, Object>> event = buildEvent(
                TypeEvent.DELETE,
                msg.getConversationId(),
                userId,
                messageId,
                Map.of("messageId", messageId));

        chatRealTimeService.sendToConversation(msg.getConversationId(), event);
    }

    //  delete only for me
    public void deleteMessageForMe(Long userId, String messageId) {
        Messages msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        msg.getDeletedFor().add(userId);
        messageRepository.save(msg);
        // Không cần push realtime vì chỉ ảnh hưởng phía client của userId
    }

    //  report
    public void reportMessage(Long userId, String messageId, String reason) {
        Messages msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        Report report = Report.builder()
                .reporterId(userId)
                .messageId(messageId)
                .conversationId(msg.getConversationId())
                .reason(reason)
                .createdAt(Instant.now())
                .build();

        reportChatRepository.save(report);
    }

    //  media helper
    public List<MessageMedia> toMessageMedia(List<Medias> results) {
        List<MessageMedia> list = new ArrayList<>();
        for (Medias r : results) {
            list.add(MessageMedia.builder()
                    .url(r.getUrl())
                    .publicId(r.getPublicId())
                    .thumbnail(r.getThumbnail())
                    .duration(r.getDuration())
                    .mediaType(r.getMediaType())
                    .build());
        }
        return list;
    }

    // search message
    public List<MessageResponse> searchMessages(Long userId, Long convId, SearchRequest request){
        Conversation conversation=conversationService.getConversationById(convId,userId);
        String keywordRegex= ".*" + Pattern.quote(request.getKeyword().trim()) + ".*";
        Set<Long> hiddenUser= blockService.getAllHiddenUserIds(userId);
        Pageable pageable=PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by("timestamp").descending()
        );
        List<Messages> messages =messageRepository.searchInConversation(convId, keywordRegex, userId,pageable);

        return messages.stream()
                .map(msg->{
                    boolean isHidden = hiddenUser.contains(msg.getSenderId());
                    return MessageResponse.builder()
                            .id(msg.getId())
                            .conversationId(msg.getConversationId())
                            .senderId(msg.getSenderId())
                            .content(isHidden ? "Tin nhắn bị ẩn do chặn" : msg.getContent())
                            .messageMedias(isHidden ? null : msg.getMediaList())
                            .timestamp(msg.getTimestamp())
                            .isEdited(msg.isEdited())
                            .seenBy(msg.getSeenBy())
                            .isPending(false)
                            .build();
                }).toList();

    }
}