package com.example.DATN.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    // userId -> set of sessionIds
    private static final Map<Long, Set<String>> onlineUsers = new ConcurrentHashMap<>();
    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = wrap(event);

        Long userId = getUserId(accessor.getUser());
        if (userId == null) {
            return;
        }
        String sessionId = accessor.getSessionId();

        boolean isNewSession = addSession(userId, sessionId);

        log.info("User {} connected with session {}", userId, sessionId);

        if (isNewSession && isFirstSession(userId)) {
            broadcastPresence(userId, "ONLINE");
        }
    }

    private boolean addSession(Long userId, String sessionId) {
        return onlineUsers
                .computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(sessionId);
    }

    private boolean isFirstSession(Long userId) {
        return onlineUsers.get(userId).size() == 1;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = wrap(event);

        Long userId = getUserId(accessor.getUser());
        if (userId == null) return;

        String sessionId = accessor.getSessionId();

        boolean isOffline = removeSession(userId, sessionId);

        log.info("User {} disconnected session {}", userId, sessionId);

        if (isOffline) {
            broadcastPresence(userId, "OFFLINE");
        }
    }

    private boolean removeSession(Long userId, String sessionId) {
        return onlineUsers.computeIfPresent(userId, (id, sessions) -> {
            sessions.remove(sessionId);
            return sessions.isEmpty() ? null : sessions;
        }) == null;
    }

    private StompHeaderAccessor wrap(SessionConnectedEvent event) {
        return StompHeaderAccessor.wrap(event.getMessage());
    }

    private StompHeaderAccessor wrap(SessionDisconnectEvent event) {
        return StompHeaderAccessor.wrap(event.getMessage());
    }

    private Long getUserId(Principal principal) {
        if (principal == null) return null;

        if (principal instanceof Authentication auth) {
            Object principalObj = auth.getPrincipal();
            if (principalObj instanceof Long userId) {
                return userId;
            }
        }

        try {
            return Long.parseLong(principal.getName());
        } catch (Exception e) {
            log.warn("Cannot parse userId from principal: {}", principal.getName());
            return null;
        }
    }

    private void broadcastPresence(Long userId, String status) {
        messagingTemplate.convertAndSend(
                "/topic/presence",
                (Object) Map.of(
                        "userId", userId,
                        "status", status
                )
        );
    }

    public static boolean isOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }

    public static int getSessionCount(Long userId) {
        return onlineUsers.getOrDefault(userId, Set.of()).size();
    }

    public static Map<Long, Set<String>> getOnlineUsers() {
        return onlineUsers;
    }
}