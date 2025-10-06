package com.example.demo.global.security;

import com.example.demo.domain.auth.service.AuthService;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.global.jwt.JwtAuthenticationFilter;
import com.example.demo.global.security.handler.JwtAuthenticationFailureHandler;
import com.example.demo.global.security.handler.JwtAuthenticationSuccessHandler;
import com.example.demo.global.jwt.service.TokenService;
import com.example.demo.global.response.RsData;
import com.example.demo.global.security.filter.LoginAuthenticationFilter;
import com.example.demo.global.security.handler.CustomAccessDeniedHandler;
import com.example.demo.global.security.handler.CustomAuthenticationEntryPoint;
import com.example.demo.global.security.handler.CustomLogoutHandler;
import com.example.demo.global.util.Ut;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final CustomLogoutHandler customLogoutHandler;
    private final AuthService authService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // {bcrypt} 등의 접두어를 자동으로 붙여주는 DelegatingPasswordEncoder
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        AuthenticationManager authManager = authenticationManager(authenticationConfiguration);

        // 커스텀 로그인 필터
        LoginAuthenticationFilter loginFilter = new LoginAuthenticationFilter(authManager, userRepository,authService);
        loginFilter.setFilterProcessesUrl("/api/auth/login"); // 로그인 URL
        loginFilter.setAuthenticationSuccessHandler(new JwtAuthenticationSuccessHandler(tokenService));
        loginFilter.setAuthenticationFailureHandler(new JwtAuthenticationFailureHandler());

        http
                // CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // H2 콘솔용 iframe 허용
                .headers(headers -> headers.frameOptions().sameOrigin())

                .cors(Customizer.withDefaults())

                // 세션/폼로그인/기본인증 비활성화 (JWT 사용)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout") // 로그아웃 처리 URL 지정
                        .addLogoutHandler(customLogoutHandler) // 커스텀 로그아웃 핸들러
                        .logoutSuccessHandler((request, response, authentication) -> {
                            // 1. refreshToken 쿠키 삭제
                            ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                                    .httpOnly(true)
                                    .secure(true) // HTTPS 환경에서는 true
                                    .path("/")
                                    .maxAge(0)
                                    .build();

                            // 2. 응답 헤더에 쿠키 삭제 설정 추가
                            response.setHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

                            // 3. 응답 상태 및 Content-Type 설정
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json;charset=UTF-8");

                            // 4. 바디 반환
                            RsData<?> rsData = new RsData<>("200", "로그아웃 완료되었습니다.");
                            String result = Ut.Json.toString(rsData);
                            response.getWriter().write(result);
                        })
                )

                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 경로 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/ping", "/error", "/favicon.ico",
                                "/actuator/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**", "/v3/api-docs/**",
                                "/css/**", "/js/**", "/images/**",
                                "/h2-console/**").permitAll()

                        // 공개 API만 선별 허용
                        .requestMatchers(
                                "/api/major/**",
                                "/api/auth/login",
                                "/api/auth/signup",
                                "/api/auth/reissue",
                                "/api/mail/**",
                                "/api/posts/**",
                                "/api/chat/**",
                                "/ws-chat/**"
                        ).permitAll()

                        // 내정보/로그아웃 등은 인증 필요
                        .requestMatchers(
                                "/api/auth/logout",
                                "/api/users/**"
                        ).authenticated()

                        .anyRequest().authenticated()
                )

                // 예외 처리
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler)          // 403
                        .authenticationEntryPoint(customAuthenticationEntryPoint) // 401
                )

                // JWT 필터: UsernamePasswordAuthenticationFilter 앞에 두기 (표준 순서)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // 로그인 필터를 UsernamePasswordAuthenticationFilter 자리에 등록
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // allowCredentials=true 인 경우, origin은 구체적으로 명시해야 함(와일드카드 불가)
        cfg.setAllowedOriginPatterns(List.of(
                "https://stg.subook.shop",
                "https://usw-bookfront-test.vercel.app",
                "https://*.vercel.app",
                "http://localhost:3000",
                "http://127.0.0.1:3000"
        ));

        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));

        // 브라우저에 노출할 응답 헤더 (Authorization 등)
        cfg.setExposedHeaders(List.of("Authorization", "Content-Disposition", "Set-Cookie"));

        cfg.setAllowCredentials(true);
        cfg.setMaxAge(Duration.ofHours(1).getSeconds());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
