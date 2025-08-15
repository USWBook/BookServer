package com.example.demo.global.security;

import com.example.demo.global.jwt.JwtAuthenticationFilter;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.jwt.handler.JwtAuthenticationFailureHandler;
import com.example.demo.global.jwt.handler.JwtAuthenticationSuccessHandler;
import com.example.demo.global.jwt.service.TokenService;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import com.example.demo.global.security.filter.LoginAuthenticationFilter;
import com.example.demo.global.security.handler.CustomAccessDeniedHandler;
import com.example.demo.global.security.handler.CustomAuthenticationEntryPoint;
import com.example.demo.global.security.handler.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtProvider jwtProvider;
    private final RedisTokenRepository redisTokenRepository;
    private final TokenService tokenService;



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
        LoginAuthenticationFilter loginFilter = new LoginAuthenticationFilter(authManager);
        loginFilter.setFilterProcessesUrl("/api/auth/login"); // 로그인 URL
        // loginFilter.setAuthenticationSuccessHandler(new LoginSuccessHandler(tokenService));
        loginFilter.setAuthenticationSuccessHandler(new JwtAuthenticationSuccessHandler(jwtProvider, redisTokenRepository));
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
                .logout(AbstractHttpConfigurer::disable)

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

}
