package com.example.demo.global.security;

import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.global.jwt.JwtAuthenticationFilter;
import com.example.demo.global.jwt.handler.JwtAuthenticationFailureHandler;
import com.example.demo.global.jwt.handler.JwtAuthenticationSuccessHandler;
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

import java.util.Arrays;
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
        LoginAuthenticationFilter loginFilter = new LoginAuthenticationFilter(authManager,userRepository);
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
                        .addLogoutHandler(customLogoutHandler) // 위에서 만든 로그아웃 핸들러 등록
                        .logoutSuccessHandler((request, response, authentication) -> {
                            // 1. refreshToken 쿠키 삭제
                            ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                                    .httpOnly(true)
                                    .secure(true) // HTTPS 환경에서는 true로 설정
                                    .path("/")
                                    .maxAge(0)
                                    .build();

                            // 2. 응답 헤더에 쿠키 삭제 설정 추가
                            response.setHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

                            // 3. 응답 상태 및 Content-Type 설정
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json;charset=UTF-8");

                            // 4. RsData 객체를 JSON으로 변환하여 응답 본문에 작성
                            RsData<?> rsData = new RsData<>("200", "로그아웃 완료되었습니다.");
                            String result = Ut.Json.toString(rsData);
                            response.getWriter().write(result);
                        })

                )

                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 경로 권한 설정
                .authorizeHttpRequests(auth -> auth
                        //  프리플라이트 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        //  루트/헬스/문서/정적 리소스/에러 공개
                        .requestMatchers(
                                "/", "/ping", "/error", "/favicon.ico",
                                "/actuator/**",
                                "/swagger-ui/**", "/v3/api-docs/**",
                                "/css/**", "/js/**", "/images/**"
                        ).permitAll()

                        // 공개 API
                        .requestMatchers(
                                "/api/major/**", "/api/auth/**", "/api/mail/**",
                                "/h2-console/**", "/api/posts/**", "/api/chat/**"
                        ).permitAll()

                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
                )

                // 예외 처리
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler)          // 403
                        .authenticationEntryPoint(customAuthenticationEntryPoint) // 401
                )


                //  JWT 필터 등록 (로그인 필터보다 앞에 두어 컨텍스트 채워넣기)
                .addFilterBefore(jwtFilter, LoginAuthenticationFilter.class)

                // 로그인 필터를 UsernamePasswordAuthenticationFilter 자리에 등록
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);



        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프론트엔드 서버의 출처(Origin)를 명시적으로 허용합니다.
        // 주소 문제인건지 aws인지 확인해야함
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000",
                "http://192.168.219.108:3000","https://usw-bookfront-test.vercel.app","https://stg.subook.shop","https://Usw-bookfront-test"));

        // 허용할 HTTP 메소드를 설정합니다.
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 허용할 요청 헤더를 설정합니다.
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 핵심: 브라우저에 노출할 헤더를 설정합니다.
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        // 자격 증명(쿠키 등)을 포함한 요청을 허용합니다.
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로("/**")에 위 CORS 설정을 적용합니다.
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
