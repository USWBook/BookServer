//    package com.example.demo.global.config.chat;
//
//    import com.example.demo.domain.user.service.CustomUserDetailsService;
//    import com.example.demo.global.jwt.JwtProvider;
//    import jakarta.servlet.http.HttpServletRequest;
//    import jakarta.servlet.http.HttpServletResponse;
//    import lombok.RequiredArgsConstructor;
//    import org.springframework.http.server.ServerHttpRequest;
//    import org.springframework.http.server.ServerHttpResponse;
//    import org.springframework.http.server.ServletServerHttpRequest;
//    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//    import org.springframework.security.core.context.SecurityContextHolder;
//    import org.springframework.security.core.userdetails.UserDetails;
//    import org.springframework.stereotype.Component;
//    import org.springframework.web.socket.WebSocketHandler;
//    import org.springframework.web.socket.server.HandshakeInterceptor;
//    import org.slf4j.Logger;
//    import org.slf4j.LoggerFactory;
//    import java.util.Map;
//
//    @Component
//    @RequiredArgsConstructor
//    public class WebSocketChatHandler implements HandshakeInterceptor {
//
//        private final JwtProvider jwtProvider;
//        private final CustomUserDetailsService userDetailsService; // UserDetailsService 구현체
//        private static final Logger log = LoggerFactory.getLogger(WebSocketChatHandler.class);
//
//        // WebSocket 핸드셰이크 직전에 호출됨. 여기서 JWT 토큰을 검증하고 인증정보를 설정.
//// @return false면 핸드셰이크 거부(연결 실패), true면 허용
//        @Override
//        public boolean beforeHandshake(ServerHttpRequest request,
//                                       ServerHttpResponse response,
//                                       WebSocketHandler wsHandler,
//                                       Map<String, Object> attributes) throws Exception {
//            log.info("[WebSocket] Handshake 시도: URI={}, RemoteAddr={}", request.getURI(), request.getRemoteAddress());
//            // HTTP 요청인지 체크 (websocket 요청은 HTTP 핸드셰이크 후에 변경됨)
//                if (!(request instanceof ServletServerHttpRequest servletRequest)) {
//                    // HTTP 요청 아니면 인증 건너뜀
//                    log.warn("[WebSocket] HTTP 요청이 아님: handshake 건너뜀");
//                    return true;
//                }
//
//            // HTTP 요청 정보 얻기
//            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
//
////            // Authorization 헤더에서 토큰 추출 (Bearer 토큰 형식 체크)
////            String authHeader = httpServletRequest.getHeader("Authorization");
////            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
////                // 토큰 없거나 형식 틀리면 401 Unauthorized 상태 반환 후 연결 거부
////                log.error("[WebSocket] Authorization 헤더 없음 또는 형식 오류: {}", authHeader);
////                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
////                return false;
////            }
////            String token = authHeader.substring(7); // "Bearer " 접두어 제외
////
////            try {
////                // JWT 토큰 유효성 검증 (서명, 만료 체크 등)
////                jwtProvider.validateToken(token);
////
////                // 토큰에서 사용자 이메일 추출
////                String email = jwtProvider.extractEmail(token);
////
////                // 이메일로 UserDetails (인증 관련 데이터) 조회
////                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
////
////                log.info("[WebSocket] 인증 성공: email={}", email);
////
////                // 인증 토큰 생성 (Spring Security 컨텍스트에 세팅하기 위한 토큰)
////                UsernamePasswordAuthenticationToken authentication =
////                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
////
////                // 인증 정보를 SecurityContext에 저장 → 이후 WebSocket 메시지에서 인증 정보 활용 가능
////                SecurityContextHolder.getContext().setAuthentication(authentication);
////
////                // WebSocket 세션 attributes에 인증 정보 저장 (필요시 활용 목적)
////                attributes.put("auth", authentication);
////
////                // 정상 인증 완료 → 핸드셰이크 허용
////                return true;
////            } catch (Exception e) {
////                // JWT 검증 실패 시 401 상태 반환 → 핸드셰이크 거부
////                log.error("[WebSocket] JWT 인증 실패: {}", e.getMessage(), e);
////                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
////                return false;
////            }
//            return  true;
//        }
//
//        /**
//         * WebSocket 핸드셰이크가 끝난 후 호출됨.
//         * 보통 리소스 정리, 로그 처리 등 후처리 작업용.
//         * 이번 구현에선 별도 작업 없어서 빈 메서드로 둠.
//         */
//        @Override
//        public void afterHandshake(ServerHttpRequest request,
//                                   ServerHttpResponse response,
//                                   WebSocketHandler wsHandler,
//                                   Exception exception) {
//            // 이 시점에는 핸드셰이크가 성공했으므로
//            // 필요하면 로그 기록, 리소스 해제, 통계 집계 등 작업 가능
//            if (exception != null) {
//                log.error("[WebSocket] Handshake 후 예외 발생: {}", exception.getMessage(), exception);
//            } else {
//                log.info("[WebSocket] Handshake 완료: URI={}", request.getURI());
//            }
//        }
//    }
