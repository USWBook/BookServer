package com.example.demo.global.jwt;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.global.exception.CustomJwtException;
import com.example.demo.global.jwt.exception.JwtUserNotFoundException;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import com.example.demo.global.response.RsData;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.lettuce.core.pubsub.PubSubOutput.Type.message;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RedisTokenRepository redisTokenRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException{

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("[JWT Filter] 토큰 없음, 필터 체인 통과");
            chain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        log.info("[JWT Filter] 요청 URI: {}", path);
        log.info("[JWT Filter] Authorization 헤더: {}", authHeader);

        String token = authHeader.replace("Bearer ", "");

        // 나중에 블랙리스트인지 로그인 기간 만료인지 바꿔야 할듯
        if (redisTokenRepository.isBlacklisted(token)) {
            log.info("[JWT Filter] 블랙리스트 토큰: {}  요청 차단.", token);
            setErrorResponse(response, 401, "더 이상 유효하지 않은 토큰 입니다.");
            return;
        }

        try {
            String email = jwtProvider.extractEmail(token);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(JwtUserNotFoundException::new);

            log.info("[JWT Filter] 사용자 인증 완료: {}", user.getEmail());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(), null,
                            List.of(new SimpleGrantedAuthority(
                                    "ROLE_" + user.getRole().name()))
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 인증 성공 시에만 다음 필터로 진행
            chain.doFilter(request, response);

        }catch (CustomJwtException e) {
                log.error("[JWT Filter] JWT 예외 발생: {}", e.getMessage());
                setErrorResponse(response, e.getStatusCode(), e.getMessage());
                // return; 은 필요 없음. 여기서 메서드 종료.
            } catch (Exception e) {
                //  그 외 알 수 없는 예외 처리
                log.error("[JWT Filter] 알 수 없는 예외 발생", e);
                setErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
            }

    }
    private void setErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        RsData<Void> rsData = new RsData<>(String.valueOf(status), message);

        String responseBody = objectMapper.writeValueAsString(rsData);

        response.getWriter().write(responseBody);
    }
}
