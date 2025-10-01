package com.example.demo.global.config.chat;

import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketChatHandler webSocketChatHandler;  // 인터셉터 주입
    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("[WebSocket] STOMP 엔드포인트 등록: /ws-chat");
        registry.addEndpoint("/ws-chat")
                .addInterceptors(webSocketChatHandler)
                .setAllowedOriginPatterns("*");
                //.withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }
}
