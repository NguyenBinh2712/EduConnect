package com.example.DATN.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

/**
 * Interceptor xác thực JWT khi client CONNECT qua WebSocket.
 *
 * Flow:
 * 1. Client gửi STOMP CONNECT với header: Authorization: Bearer <token>
 * 2. Interceptor này bắt message CONNECT, decode JWT, lấy userId
 * 3. Set Principal là một object chứa userId (Long dạng String)
 * 4. WebSocketEventListener.getUserId() đọc được userId từ principal.getName()
 *
 * Kết quả: principal.getName() = "123" (userId dạng String)
 * → WebSocketEventListener parse thành Long OK
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements WebSocketMessageBrokerConfigurer {

    private final CustomJwtDecoder customJwtDecoder;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor == null) return message;

                // Chỉ xử lý lúc CONNECT
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        try {
                            Jwt jwt = customJwtDecoder.decode(token);

                            // Lấy userId từ claim "userId" trong token
                            Long userId = ((Number) jwt.getClaim("userId")).longValue();

                            // Set principal = userId dạng String
                            // → WebSocketEventListener.getUserId() sẽ parse được
                            accessor.setUser(new UserPrincipal(userId));

                        } catch (Exception e) {

                        }
                    }
                }

                return message;
            }
        });
    }


    public record UserPrincipal(Long userId) implements Principal {
        @Override
        public String getName() {
            return String.valueOf(userId);
        }
    }
}