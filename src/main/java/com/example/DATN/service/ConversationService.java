package com.example.DATN.service;

import com.example.DATN.dto.chat.ConversationResponse;
import com.example.DATN.dto.chat.CreateChatGroup;
import com.example.DATN.dto.chat.ReportRequest;
import com.example.DATN.entity.Conversation;
import com.example.DATN.entity.Participant;
import com.example.DATN.entity.User;
import com.example.DATN.entity.document.Messages;
import com.example.DATN.entity.document.Report;
import com.example.DATN.entity.enums.ChatReportType;
import com.example.DATN.entity.enums.ConversationStatus;
import com.example.DATN.entity.enums.MembershipRole;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ConversationService {
    ConversationRepository conversationRepository;
    UserRepository userRepository;
    ParticipantRepository participantRepository;
    FriendshipRepository friendshipRepository;
    ReportChatRepository reportChatRepository;

    // create or get conversation 1 vs 1
    public ConversationResponse createOrGetOneToOne(Long userId1,Long userId2){
        if(userId1.equals(userId2)){
            throw new AppException(ErrorCode.CANNOT_CHAT_WITH_SELF);
        }
        Conversation conv=conversationRepository.findConversationOneToOne(userId1,userId2)
                .orElseGet(()->createNewConversation(userId1,userId2));
        return toConversationResponse(conv,userId1);
    }

    //create conversation 1vs1
    private Conversation createNewConversation(Long userId1,Long userId2){
        boolean isFriend=friendshipRepository.existsFriendship(userId1,userId2);
        Conversation conv= Conversation.builder()
                .isGroup(false)
                .createdAt(LocalDateTime.now())
                .status(isFriend? ConversationStatus.NORMAL:ConversationStatus.PENDING)
                .build();
        conv.addParticipant(userRepository.findById(userId1).orElseThrow(),MembershipRole.MEMBER);
        conv.addParticipant(userRepository.findById(userId2).orElseThrow(),MembershipRole.MEMBER);
        conversationRepository.save(conv);
        return conv;
    }
    // map to conversationResponse
    private ConversationResponse toConversationResponse(Conversation conv,Long userId){
        long unReadCount=0;
        return ConversationResponse.builder()
                .id(conv.getId())
                .name(conv.getName())
                .isGroup(conv.isGroup())
                .isPending(conv.isPending())
                .lastMessagePreview(conv.getLastMessagePreview())
                .lastMessageAt(conv.getLastMessageAt())
                .unreadCount(unReadCount)
                .build();
    }

    public Conversation getConversationById(Long conversationId,Long userId){
        Conversation conv= conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        assertParticipant(userId,conv);
        return conv;
    }

    //xac dinh user trong conv
    public void assertParticipant(Long userId, Conversation conv) {
        if (!conv.isParticipant(userId)) {
            throw new AppException(ErrorCode.NOT_CONVERSATION_MEMBER);
        }
    }

    //update msg cuoi cung
    public void updateLastMessage(Conversation conv, Messages message){
        conv.setLastMessagePreview(message.getContent());
        conv.setLastMessageAt(message.getTimestamp());
        conversationRepository.save(conv);
    }

    //create Group
    public ConversationResponse createGroupChat(Long creatorId, CreateChatGroup req) {

        if (req.getMemberId() == null || req.getMemberId().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Conversation group = Conversation.builder()
                .name(req.getName() != null ? req.getName() : "Group chat")
                .isGroup(true)
                .createdAt(LocalDateTime.now())
                .build();

        // OWNER
        group.addParticipant(creator, MembershipRole.OWNER);

        Set<Long> ids = req.getMemberId().stream()
                .filter(id -> !id.equals(creatorId))
                .collect(Collectors.toSet());

        List<User> users = userRepository.findAllById(ids);

        if (users.size() != ids.size()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        users.forEach(u -> group.addParticipant(u, MembershipRole.MEMBER));

        conversationRepository.save(group);

        return toConversationResponse(group, creatorId);
    }


    //add member to group
    public void addParticipant(Long convId, Long currentUserId, List<Long> membersId){
        Conversation conv = conversationRepository.findById(convId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        if (membersId == null || membersId.isEmpty()) return;
        if (!conv.isParticipant(currentUserId)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        Set<Long> existing = conv.getParticipants().stream()
                .map(p -> p.getUser().getId())
                .collect(Collectors.toSet());
        Set<Long> newIds = membersId.stream()
                .filter(id -> !existing.contains(id))
                .collect(Collectors.toSet());
        if (newIds.isEmpty()) return;
        List<User> newMembers = userRepository.findAllById(newIds);
        if (newMembers.size() != newIds.size()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        newMembers.forEach(mem->conv.addParticipant(mem,MembershipRole.MEMBER));
        if (!conv.isGroup() && conv.getParticipants().size() >= 3) {
            conv.setGroup(true);
            conv.setName("Group chat");
        }
        conversationRepository.save(conv);
    }

    // remove member from group
    public void removeMember(Long convId, Long userId, List<Long> memberIds) {

        Conversation conv = conversationRepository.findById(convId)
                .orElseThrow(()->new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        if (!conv.isGroup()) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        assertOwner(conv, userId);
        List<Participant> participants = memberIds.stream()
                .map(id -> participantRepository
                        .findByConversationIdAndUserId(convId, id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_CONVERSATION_MEMBER)))
                .toList();

        if (participants.isEmpty()) return;

        for (Participant p : participants) {
            if (p.getMembershipRole() == MembershipRole.OWNER) {
                throw new AppException(ErrorCode.CANNOT_REMOVE_OWNER);
            }
        }

        participantRepository.deleteAll(participants);

        conv.getParticipants().removeAll(participants);

        conversationRepository.save(conv);
    }

    // out group
    public void leaveGroup(Long convId, Long userId) {

        Conversation conv = conversationRepository.findById(convId)
                .orElseThrow(()->new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        if(!conv.isGroup()){
            return;
        }

        Participant p = participantRepository
                .findByConversationIdAndUserId(convId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_CONVERSATION_MEMBER));

        if (p.getMembershipRole() == MembershipRole.OWNER) {
            throw new AppException(ErrorCode.OWNER_CANNOT_LEAVE);
        }

        participantRepository.delete(p);
        conv.getParticipants().remove(p);

        conversationRepository.save(conv);
    }

    // gán quyền sở hữu cho 1 tv nào đó
    public void promoteToOwner(Long convId, Long currentUserId, Long targetId) {

        Conversation conv = conversationRepository.findById(convId)
                .orElseThrow(()->new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        assertOwner(conv, currentUserId);
        Participant p = participantRepository
                .findByConversationIdAndUserId(convId, targetId)
                .orElseThrow();

        p.setMembershipRole(MembershipRole.OWNER);
        participantRepository.save(p);
    }

    // xác định chủ group
    private void assertOwner(Conversation conv, Long userId) {
        Participant p = participantRepository
                .findByConversationIdAndUserId(conv.getId(), userId)
                .orElseThrow(()->new AppException(ErrorCode.NOT_CONVERSATION_MEMBER));

        if (p.getMembershipRole() != MembershipRole.OWNER) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
    }

    //accept conv isPending
    public void acceptConversation(Long userId,Long convId){
        Conversation conversation=getConversationById(convId,userId);
        conversation.setStatus(ConversationStatus.NORMAL);
        conversationRepository.save(conversation);
        // xu li them
    }

    //reject conv isPending
    public void rejectConversation(Long userId,Long convId){
        Conversation conv=getConversationById(convId,userId);
        conv.setStatus(ConversationStatus.REJECT);
        conversationRepository.save(conv);
    }

    //delete conv
    public void deleteConversation(Long userId,Long convId){
        Conversation conv=getConversationById(convId,userId);
        assertParticipant(userId,conv);
        Participant par=participantRepository.findByConversationIdAndUserId(convId,userId)
                .orElseThrow(()->new AppException(ErrorCode.NOT_CONVERSATION_MEMBER));
        par.setDeleted(true);
        par.setDeleteAt(Instant.now());
        participantRepository.save(par);
    }

    // archive or unArchive conversation
    public void archiveConversation(Long userId,Long convId){
        Participant p = participantRepository
                .findByConversationIdAndUserId(convId, userId)
                .orElseThrow();

        p.setArchived(true);
        participantRepository.save(p);
    }

    public void unArchiveConversation(Long userId,Long convId){
        Participant p = participantRepository
                .findByConversationIdAndUserId(convId, userId)
                .orElseThrow();

        p.setArchived(false);
        participantRepository.save(p);
    }

    //get conv archive
    public List<ConversationResponse> getConversationArchive(Long userId){
        List<Conversation> conversations=conversationRepository.findAllConversationByUserId(userId);
        return conversations.stream()
                .filter(c->{
                    Participant p=participantRepository.findByConversationIdAndUserId(c.getId(),userId)
                            .orElseThrow(()->new AppException(ErrorCode.NOT_CONVERSATION_MEMBER));
                   return p!=null && p.isArchived();
                })
                .map(c->toConversationResponse(c,userId))
                .toList();
    }

    // report conv
    public void reportConversation(Long userId,ReportRequest request){
        if(!request.getType().equals(ChatReportType.CONVERSATION)){
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        Conversation conv=getConversationById(request.getConvId(), userId);
        Report report=Report.builder()
                .reporterId(userId)
                .conversationId(request.getConvId())
                .type(request.getType())
                .reason(request.getReason())
                .createdAt(Instant.now())
                .build();
        reportChatRepository.save(report);
    }

    public Slice<ConversationResponse> getMyConversations(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return conversationRepository
                .findAllConversationByUserId(userId, pageable)
                .map(conv -> toConversationResponse(conv, userId));
    }

//    public List<Report> getReportConversation(){
//        return
//    }

//    search conversation
//    public List<ConversationResponse> searchConversation()
}
