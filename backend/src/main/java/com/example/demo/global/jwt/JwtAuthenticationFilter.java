package com.example.demo.global.jwt;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RedisTokenRepository redisTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException{
        String path = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        log.info("[JWT Filter] 요청 URI: {}", path);
        log.info("[JWT Filter] Authorization 헤더: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("[JWT Filter] 토큰 없음, 필터 체인 통과");
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.replace("Bearer ", "");

        if (redisTokenRepository.isBlacklisted(token)) {
            log.info("[JWT Filter] 블랙리스트 토큰: {}  요청 차단.", token);
            chain.doFilter(request, response);
            return;
        }

        try {
            String email = jwtProvider.extractEmail(token);

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                log.info("[JWT Filter] 사용자 {} 없음", email);
            } else {
                log.info("[JWT Filter] 사용자 인증 완료: {}", user.getEmail());
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                        user.getEmail(),null,
                        List.of(new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole()
                                )
                        )
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        }catch (JwtException e){
            log.error("[JWT Filter] JWT 예외: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }
}
