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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private static final Logger log = LoggerFactory.getLogger(StompAuthChannelInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        // 인증이 필요한 명령만 처리: CONNECT, SEND, SUBSCRIBE
        if (command != StompCommand.CONNECT &&
                command != StompCommand.SEND &&
                command != StompCommand.SUBSCRIBE) {
            return message; // 그 외 프레임은 그냥 통과
        }

        try {
            log.info("🌐 STOMP {} 명령 수신. 인증 시작.", command);

            List<String> authHeaders = accessor.getNativeHeader("Authorization");

            if (authHeaders == null || authHeaders.isEmpty()) {
                log.warn("❌ Authorization header가 누락되었습니다. 인증 건너뜀.");
                return message; // 예외를 던지지 않고 건너뜀
            }

            String token = authHeaders.get(0);
            if (!token.startsWith("Bearer ")) {
                log.warn("❌ Authorization header 형식이 잘못되었습니다: {}", token);
                return message; // 형식 잘못되면 인증 건너뜀
            }

            String jwt = token.substring(7);

            // JWT 유효성 검증
            jwtProvider.validateToken(jwt);

            // 이메일 추출 및 사용자 조회
            String email = jwtProvider.extractEmail(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Authentication 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // STOMP 세션에 Principal 설정
            accessor.setUser(authentication);

            log.info("✅ STOMP 인증 성공: 이메일={}", email);

        } catch (Exception e) {
            log.error("🚨 STOMP 인증 오류: {}", e.getMessage(), e);
            // 인증 실패 시 Principal은 null, 메시지는 계속 전달
        }

        return message;
    }
}