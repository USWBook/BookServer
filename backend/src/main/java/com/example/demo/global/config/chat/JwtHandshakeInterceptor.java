package com.example.demo.global.config.chat;

import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.domain.user.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private static final Logger log = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);


    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // HTTP 요청 헤더에서 Authorization 토큰 가져오기
        List<String> authHeaders = request.getHeaders().get("Authorization");

        if (authHeaders == null || authHeaders.isEmpty()) {
            log.warn("Handshake: Authorization header가 누락되었습니다.");
            return false;  // 인증 실패시 연결 거부
        }

        String token = authHeaders.get(0);

        if (!token.startsWith("Bearer ")) {
            log.warn("Handshake: Authorization header 형식이 올바르지 않습니다: {}", token);
            return false;
        }

        String jwt = token.substring(7);
        try {
            jwtProvider.validateToken(jwt);
            String email = jwtProvider.extractEmail(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // Handshake attributes에 Authentication 객체 저장해둠
            attributes.put("auth", authentication);

            log.info("Handshake: JWT 인증 성공, email={}", email);

            return true;  // 인증 성공하면 연결 허용
        } catch (Exception e) {
            log.error("Handshake: JWT 인증 실패 - {}", e.getMessage());
            return false;  // 인증 실패 시 연결 거부
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 핸드셰이크 후 별도 처리 필요 없으면 빈 메서드로 둠
    }

}
