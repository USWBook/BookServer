package com.example.demo.global.config.chat;

import com.example.demo.domain.user.service.CustomUserDetailsService;
import com.example.demo.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private static final Logger log = LoggerFactory.getLogger(StompAuthChannelInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);

        final String SESSION_USER_KEY = "simpUser";

        if (accessor.getCommand() == null) {
                return message;
        }

        // ✅ CONNECT 프레임에서만 인증 수행
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            try {
                log.info("🌐 STOMP CONNECT 요청 수신. 헤더: {}", accessor.toNativeHeaderMap());

                String authHeader = accessor.getFirstNativeHeader("Authorization");

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    log.warn("❌ Authorization 헤더가 없거나 잘못된 형식입니다. authHeader={}", authHeader);
                    return message;
                }

                String token = authHeader.substring(7);

                // JWT 검증
                if (!jwtProvider.isValid(token)) {
                    log.warn("❌ JWT 토큰이 유효하지 않습니다.");
                    return message;
                }

                // 이메일 추출 및 사용자 조회
                String email = jwtProvider.extractEmail(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Authentication 생성 및 Principal 주입
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                accessor.setUser(authentication); // ✅ 핵심: Principal 세팅
                accessor.getSessionAttributes().put(SESSION_USER_KEY, authentication);

                log.info("✅ STOMP 인증 성공: 이메일={}", email);

            } catch (Exception e) {
                log.error("🚨 STOMP 인증 오류: {}", e.getMessage(), e);
            }
        }

        else if (accessor.getCommand() != null) {
            // 세션에서 Principal을 가져옴
            Object userPrincipal = accessor.getSessionAttributes().get(SESSION_USER_KEY);

            if (userPrincipal instanceof Principal) {
                // 가져온 Principal을 현재 메시지에 설정 (SecurityContextChannelInterceptor가 읽을 수 있도록)
                accessor.setUser((Principal) userPrincipal);
            }
        }

        return message;
    }
}