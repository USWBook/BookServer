package com.example.demo.global.jwt;

import com.example.demo.domain.auth.exception.BannedUserException;
import com.example.demo.domain.user.role.Role;
import com.example.demo.global.exception.CustomJwtException;
import com.example.demo.global.jwt.exception.JwtInvalidSignatureException;
import com.example.demo.global.jwt.service.TokenService;
import com.example.demo.global.security.userdetails.CustomUserDetails;
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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final TokenService tokenService;

    /**
     * 이 필터는 HTTP 요청이 들어올 때마다 실행됩니다. (OncePerRequestFilter)
     * 역할: HTTP 요청 헤더에서 JWT Access Token을 감지하고, 토큰이 유효하다면
     * SecurityContext에 인증 정보를 설정하여 해당 요청 동안 사용자가 인증된 것으로 간주하게 만듭니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String[] skipPaths = {
                "/", "/ping", "/error", "/favicon.ico",
                "/actuator/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/api/auth/**",
                "/api/major/**",
                "/api/mail/**",
                "/api/files/image"
        };

        String path = request.getRequestURI();

        boolean shouldSkip = Arrays.stream(skipPaths)
                .anyMatch(p -> antPathMatcher.match(p, path));

        if (shouldSkip) {
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
            if (tokenService.isBlacklisted(token)) throw new BannedUserException();

            UUID userId = jwtProvider.extractId(token);
            String email = jwtProvider.extractEmail(token);
            Role role = jwtProvider.extractRole(token);

            // 이방식은 DB를 조회 해야해서 성능이 떨어짐. 하지만 보안적으론 좋음
//            User user = userRepository.findByEmail(email)
//                    .orElseThrow(JwtUserNotFoundException::new);

            // CustomUserDetails 객체를 생성(DB 조회 없이)하여 인증 주체(principal)로 사용
            CustomUserDetails customUserDetails = new CustomUserDetails(userId,email, role);

            /*
             * [핵심] 인증 객체 생성
             *
             * JWT가 성공적으로 검증되었으므로, 우리는 이 사용자를 '인증된 사용자'로 간주합니다.
             * 비밀번호가 없으므로 credentials 인자는 null을 전달합니다.
             * 이 Authentication 객체는 '이미 인증이 완료된 상태'임을 나타냅니다.
             * 여기서는 AuthenticationManager를 통한 인증 절차가 전혀 필요 없습니다.
             */
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

            // ---  SecurityContext에 인증 정보 등록 ---
            /*
             * SecurityContextHolder는 현재 실행 중인 스레드의 보안 컨텍스트를 관리합니다.
             * 여기에 인증 정보를 설정하면, 현재 요청을 처리하는 동안 @AuthenticationPrincipal 등을 통해
             * 컨트롤러나 서비스 레이어에서 인증된 사용자 정보에 접근할 수 있게 됩니다.
             * 이것이 바로 "상태 없는(stateless)" 인증을 구현하는 핵심 부분입니다.
             */
            SecurityContextHolder.getContext().setAuthentication(authentication);



        } catch (CustomJwtException e) {
            // 토큰 검증 과정에서 발생한 모든 예외를 처리
            SecurityContextHolder.clearContext(); // 컨텍스트를 깨끗하게 비움
            // CustomAuthenticationEntryPoint를 통해 클라이언트에게 401 응답을 보냄
            authenticationEntryPoint.commence(request, response, new BadCredentialsException(e.getMessage(), e));
            return; // 예외 발생 시 필터 체인 중단
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