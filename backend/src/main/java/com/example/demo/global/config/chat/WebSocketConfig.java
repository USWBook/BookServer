package com.example.demo.global.config.chat;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.context.ApplicationContext;

import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.context.ApplicationContext;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);
    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;
    private final ApplicationContext applicationContext;
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("[WebSocket] STOMP 엔드포인트 등록: /ws-chat");
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*");
        //.withSockJS();  // SockJS 사용시 주석 해제
    }

    //sub 경로: 구독용, pub 경로: 메시지를 보낼 때 사용
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    //JWT 인증/인가 처리를 수행
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
        registration.interceptors(new SecurityContextChannelInterceptor());
    }
}
