package com.example.demo.global.jwt;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.global.exception.CustomJwtException;
import com.example.demo.global.jwt.exception.JwtBlacklistedException;
import com.example.demo.global.jwt.exception.JwtTokenExpiredException;
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
    //private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException{

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("[JWT Filter] Authorization 헤더 없음 또는 잘못됨 → 인증 없이 통과");
            chain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        log.info("[JWT Filter] 요청 URI: {}", path);
        log.info("[JWT Filter] Authorization 헤더: {}", authHeader);

        String token = authHeader.replace("Bearer ", "");

        try {

            // 블랙리스트 체크
            if (redisTokenRepository.isBlacklisted(token)) {
                log.info("[JWT Filter] 블랙리스트 토큰: {}  요청 차단.", token);
                throw new JwtBlacklistedException();
            }
            String email = jwtProvider.extractEmail(token);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(JwtUserNotFoundException::new);

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
                // 이거 로그인 안되어 있거나 토큰이 만료되면 예외 발생 시키고 이걸 프론트가 받으면 프론트에서 리다이렉트 시켜야함
            throw new JwtTokenExpiredException();
            }
    }
//    private void setErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
//        response.setStatus(status);
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        response.setCharacterEncoding("UTF-8");
//
//        RsData<Void> rsData = new RsData<>(String.valueOf(status), message);
//
//        String responseBody = objectMapper.writeValueAsString(rsData);
//
//        response.getWriter().write(responseBody);
//    }
}
