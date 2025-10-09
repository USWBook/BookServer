package com.example.demo.global.security.handler;

import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.jwt.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Optional;


@Component
@RequiredArgsConstructor
@Slf4j
public class CustomLogoutHandler implements LogoutHandler {

    private final TokenService tokenService;
    private final JwtProvider jwtProvider;
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String authHeader = request.getHeader("Authorization");

        Optional<String> refreshTokenOpt = extractCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);

        //만약 액세스 토큰이없다면
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {

            // refreshToken이 쿠키에 존재하는 경우 삭제 로직을 실행.
            refreshTokenOpt.ifPresent(refreshToken -> {
                log.info("Access Token 없이 Refresh Token으로 로그아웃을 처리합니다.");
                try {
                    // Refresh Token 자체의 유효성 검증도 수행하는 것이 안전.
                    jwtProvider.parse(refreshToken);
                    String email = jwtProvider.extractEmail(refreshToken);

                    // 여기에 refreshToken을 사용하여 Redis에서 삭제하는 로직 구현
                    tokenService.deleteRefreshToken(email);

                } catch (JwtException | IllegalArgumentException e) {
                    log.warn("로그아웃 시도 중 유효하지 않은 Refresh Token 쿠키가 감지되었습니다.", e);
                }

            });
            // 리프레시 토큰이 있던 없던 종료
            return;
        }

        // 액세스 토큰이 있다면
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

    /**
     * HttpServletRequest에 포함된 쿠키에서 특정 이름의 쿠키 값을 추출합니다.
     *
     * @param request      HttpServletRequest 객체
     * @param cookieName   찾고자 하는 쿠키의 이름
     * @return 해당 쿠키의 값을 담은 Optional 객체. 쿠키가 없으면 Optional.empty() 반환.
     */
    private Optional<String> extractCookieValue(HttpServletRequest request, String cookieName) {
        // 1. 요청에서 모든 쿠키를 가져옵니다. 쿠키가 없으면 null일 수 있습니다.
        Cookie[] cookies = request.getCookies();

        // 2. 쿠키 배열이 null이거나 비어있는지 확인합니다.
        if (cookies == null || cookies.length == 0) {
            return Optional.empty();
        }

        // 3. 스트림을 사용하여 원하는 이름의 쿠키를 찾습니다.
        return Arrays.stream(cookies)                // Cookie 배열을 스트림으로 변환
                .filter(cookie -> cookieName.equals(cookie.getName())) // 이름이 일치하는 쿠키만 필터링
                .map(Cookie::getValue)               // 쿠키 객체에서 값(토큰 문자열)만 추출
                .findFirst();                        // 첫 번째로 발견된 요소 반환 (Optional<String>)
    }
}