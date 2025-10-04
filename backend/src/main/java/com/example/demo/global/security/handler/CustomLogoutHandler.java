package com.example.demo.global.security.handler;

import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.jwt.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


@Component
@RequiredArgsConstructor
@Slf4j
public class CustomLogoutHandler implements LogoutHandler {

    private final TokenService tokenService;
    private final JwtProvider jwtProvider;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String authHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            // 토큰이 없는 경우, 처리할 것도 없으므로 그냥 종료
            log.info("로그아웃 요청에 Access Token이 없습니다.");
            return;
        }

        String token = authHeader.substring(7);

        try {
            // Case 1: 토큰이 유효한 경우 (만료되지 않음, 서명 정상)
            jwtProvider.parse(token); // parse()는 내부적으로 모든 검증을 수행하고, 실패 시 예외를 던집니다.

            // 액세스 토큰을 블랙리스트 처리함
            tokenService.blacklistToken(token);
            // 리프레시토큰을 레디스에서 삭제함.
            String email = jwtProvider.extractEmail(token);
            tokenService.deleteRefreshToken(email);

        } catch (ExpiredJwtException e) {
            // Case 2: 토큰이 만료된 경우
            log.info("만료된 Access Token으로 로그아웃을 시도했습니다.");
            String email = jwtProvider.extractEmailFromExpiredToken(token);
            tokenService.deleteRefreshToken(email);

        } catch (JwtException | IllegalArgumentException e) {
            // Case 3: 토큰이 위변조되었거나(SignatureException) 형식이 잘못된 경우(MalformedJwtException)
            log.warn("로그아웃 시도 중 유효하지 않은 토큰이 감지되었습니다. token: {}", token, e);
            // 이 경우, 해당 토큰으로 할 수 있는 작업이 없으므로 아무것도 하지 않고 조용히 종료합니다.
            // 어차피 성공 핸들러가 쿠키 삭제 등 마무리 작업을 해줄 것입니다.
        }
    }
}