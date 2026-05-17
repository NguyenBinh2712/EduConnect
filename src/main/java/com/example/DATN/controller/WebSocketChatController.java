package com.example.DATN.controller;

import com.example.DATN.dto.chat.*;
import com.example.DATN.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Collections;

@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final ChatService chatService;

    // send messages
    @MessageMapping("/chat.send/{convId}")
    public void sendMessage(@DestinationVariable Long convId,
                            @Payload SendMessageRequest request,
                            Principal principal) {
        Long userId = parseUserId(principal);
        if (userId == null) return;

        request.setConversationId(convId);
        chatService.sendMessages(userId, request, Collections.emptyList());
    }

    // typing
    @MessageMapping("/chat.typing/{convId}")
    public void typing(@DestinationVariable Long convId,
                       Principal principal) {
        Long userId = parseUserId(principal);
        if (userId == null) return;
        chatService.typing(userId, convId);
    }

    // mark messages
    @MessageMapping("/chat.seen/{convId}")
    public void seen(@DestinationVariable Long convId,
                     Principal principal) {
        Long userId = parseUserId(principal);
        if (userId == null) return;
        chatService.markMessages(convId, userId);
    }

    // reaction messages
    @MessageMapping("/chat.react")
    public void react(@Payload ReactionMessageRequest request,
                      Principal principal) {
        Long userId = parseUserId(principal);
        if (userId == null) return;
        chatService.reactionMessage(userId, request);
    }

    // edit messages
    @MessageMapping("/chat.edit")
    public void editMessage(@Payload WsEditMessageRequest request,
                            Principal principal) {
        Long userId = parseUserId(principal);
        if (userId == null) return;
        chatService.editMessage(userId, request.getMessageId(), request.getContent());
    }

   // delete message
    @MessageMapping("/chat.delete")
    public void deleteMessage(@Payload WsDeleteMessageRequest request,
                              Principal principal) {
        Long userId = parseUserId(principal);
        if (userId == null) return;
        chatService.deleteMessage(userId, request.getMessageId());
    }

   //reply
    @MessageMapping("/chat.reply/{convId}")
    public void replyMessage(@DestinationVariable Long convId,
                             @Payload RepplyMessage request,
                             Principal principal) {
        Long userId = parseUserId(principal);
        if (userId == null) return;
        request.getSendRequest().setConversationId(convId);
        chatService.replyMessage(userId, request);
    }


    private Long parseUserId(Principal principal) {
        if (principal == null) return null;
        try {
            return Long.parseLong(principal.getName());
        } catch (Exception e) {
            return null;
        }
    }
}