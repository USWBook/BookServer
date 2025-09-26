package com.example.demo.global.jwt;

import com.example.demo.domain.auth.exception.BannedUserException;
import com.example.demo.domain.user.role.Role;
import com.example.demo.global.exception.CustomJwtException;
import com.example.demo.global.jwt.exception.JwtInvalidSignatureException;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import com.example.demo.domain.user.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final RedisTokenRepository redisTokenRepository;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // ① 액추에이터/헬스는 바로 통과 (의존성 안 타고 빠르게)
        String path = request.getRequestURI();
        if (path.startsWith("/actuator/")) {
            chain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        if (token == null) {
            chain.doFilter(request, response);
            return;
        }


        try {

            // 토큰이 만료되면 401 코드
            jwtProvider.parse(token);

            // access토큰이 아니면 401
            if(!Objects.equals(jwtProvider.getCategory(token), "access")) throw new JwtInvalidSignatureException();
            // 블랙리스트 체크
            if (redisTokenRepository.isBlacklisted(token)) throw new BannedUserException();

            UUID userId = jwtProvider.extractId(token);
            String email = jwtProvider.extractEmail(token);
            Role role = jwtProvider.extractRole(token);

            // 이방식은 DB를 조회 해야해서 성능이 떨어짐. 하지만 보안적으론 좋음
//            User user = userRepository.findByEmail(email)
//                    .orElseThrow(JwtUserNotFoundException::new);

            // UserDetails 구현체 생성 (DB 조회 없이)
            CustomUserDetails customUserDetails = new CustomUserDetails(userId,email, role);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);



        } catch (CustomJwtException e) {
            // (선택) 기존처럼 throw로 전파해도 되지만,
            // 헬스체크/프록시와 궁합을 위해 401 응답으로 종료하는 걸 권장
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, new BadCredentialsException(e.getMessage(), e));
            return;
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//            response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"" + e.getMessage() + "\"}");
            // throw new JwtTokenExpiredException();
        }
        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}