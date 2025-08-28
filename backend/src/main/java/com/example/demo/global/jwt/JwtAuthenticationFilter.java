package com.example.demo.global.jwt;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.role.Role;
import com.example.demo.global.exception.CustomJwtException;
import com.example.demo.global.jwt.exception.JwtInvalidSignatureException;
import com.example.demo.global.jwt.exception.JwtTokenExpiredException;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import com.example.demo.domain.user.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final RedisTokenRepository redisTokenRepository;



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

        // ② 토큰 없으면 통과
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring("Bearer ".length());

        // 토큰이 만료되면 401 코드
        try {
            jwtProvider.isExpired(token);
        } catch (Exception e) {

            throw new JwtTokenExpiredException();
        }

        // access토큰이 아니면 401
        if(!Objects.equals(jwtProvider.getCategory(token), "access")) throw new JwtInvalidSignatureException();

        try {
            // 블랙리스트 체크
            if (redisTokenRepository.isBlacklisted(token)) {
                // (선택) 401로 응답하고 종료
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"블랙리스트 토큰\"}");
                return;
            }

            String email = jwtProvider.extractEmail(token);
            Role role = jwtProvider.extractRole(token);

            // 이방식은 DB를 조회 해야해서 성능이 떨어짐. 하지만 보안적으론 좋음
//            User user = userRepository.findByEmail(email)
//                    .orElseThrow(JwtUserNotFoundException::new);

            // UserDetails 구현체 생성 (DB 조회 없이)
            User user = new User();
            user.setEmail(email);
            user.setRole(role);

            CustomUserDetails customUserDetails = new CustomUserDetails(user);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);

        } catch (CustomJwtException e) {
            // (선택) 기존처럼 throw로 전파해도 되지만,
            // 헬스체크/프록시와 궁합을 위해 401 응답으로 종료하는 걸 권장
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"" + e.getMessage() + "\"}");
            // throw new JwtTokenExpiredException();
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
