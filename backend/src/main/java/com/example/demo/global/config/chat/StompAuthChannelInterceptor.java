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
    import org.springframework.security.core.context.SecurityContextHolder;
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

            // CONNECT 명령이 아니면 인증 처리를 건너뜁니다.
            if (!StompCommand.CONNECT.equals(accessor.getCommand())) {
                return message;
            }

            // --- CONNECT 인증 로직 시작 ---
            try {
                log.info("🌐 STOMP CONNECT 명령 수신. 인증 시작.");

                List<String> authHeaders = accessor.getNativeHeader("Authorization");

                if (authHeaders == null || authHeaders.isEmpty()) {
                    log.warn("❌ Authorization header가 누락되었습니다. (토큰이 전달되지 않음)");
                    // 토큰이 없으면 인증 없이 계속 진행하거나 (null Principal), 예외 발생으로 연결을 끊을 수 있습니다.
                    // 여기서는 예외를 던져 연결을 끊고 클라이언트에게 오류를 알립니다.
                    throw new SecurityException("Authorization header is missing");
                }

                String token = authHeaders.get(0);
                if (!token.startsWith("Bearer ")) {
                    log.warn("❌ Authorization header 형식이 잘못되었습니다: {}", token);
                    throw new SecurityException("Invalid Authorization header format");
                }
                String jwt = token.substring(7);

                log.info("토큰 추출 완료: {}", jwt);

                // 1. JWT 유효성 검증 (여기서 만료/변조 시 예외 발생 가능)
                jwtProvider.validateToken(jwt);

                // 2. 이메일 추출 및 사용자 조회 (이메일로 사용자를 찾지 못하면 예외 발생 가능)
                String email = jwtProvider.extractEmail(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 3. Authentication 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // 4. Stomp 세션에 Principal 설정 (이것이 @MessageMapping에 Principal을 전달하는 핵심)
                accessor.setUser(authentication);

                log.info("✅ STOMP 인증 성공: 이메일={}", email);

            } catch (SecurityException | IllegalArgumentException e) {
                // 토큰 관련 자체 예외 처리 (e.g., 토큰 없음, 형식 오류)
                log.error("🚨 STOMP CONNECT 인증 실패 (Client Error): {}", e.getMessage());

                throw new RuntimeException("STOMP Authentication Failed: " + e.getMessage());

            } catch (Exception e) {
                // DB 조회 또는 JWTProvider 내부의 예상치 못한 예외 처리
                log.error("🚨 STOMP CONNECT 예상치 못한 오류: {}", e.getMessage(), e);
                // 인증 실패 시 Principal 설정을 건너뜁니다.
                throw new RuntimeException("STOMP Unexpected Error", e);
            }

            // SecurityContextHolder.getContext().setAuthentication(authentication);

            // Principal 설정에 성공했거나 실패했더라도 메시지는 다음 체인으로 전달됩니다.
            return message;
        }
    }