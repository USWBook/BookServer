package com.example.demo.global.security;

import com.example.demo.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // authManager Bean을 얻기 위한 authConfiguration 객체
    private final AuthenticationConfiguration authenticationConfiguration;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // get authManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF Disable
                .csrf(AbstractHttpConfigurer::disable)

                // iframe 허용 (H2 Console 등)
                .headers(headers -> headers.frameOptions().sameOrigin()) // ✅ 추가

                .cors(Customizer.withDefaults())

                // Session login disable
                .formLogin(AbstractHttpConfigurer::disable)    // UsernamePasswordAuthenticationFilter disable
                .httpBasic(AbstractHttpConfigurer::disable)    // 기본 로그인창 disable

                // 로그아웃 필터 비활성화
                .logout(AbstractHttpConfigurer::disable)

                // 세션 정보를 저장하지 않음(jwt에서는 임시 세션 정보 사용, 사용된 세션은 이후 초기화)
                .sessionManagement(httpSecuritySessionManagementConfigurer -> {
                    httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })

//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(SecurityConstants.AUTH_WHITELIST.toArray(String[]::new)).permitAll()
//                        .anyRequest().permitAll() // 모든 요청 인증 없이 허용 (임시)

                // 정용현 테스트용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/mail/**", "/h2-console/**","/api/posts/**").permitAll()
                        .anyRequest().authenticated()
                )

                // UsernamePasswordAuthenticationFilter는 비활성화 되어있고
                // JWT 필터는 인증 정보를 확인하고, SecurityContext에 저장하는 필터
                // 따라서, SecurityContextHolderFilter가 실행되기 전에 인증 객체를 넣어야 함
                // SecurityContextHolderFilter 앞에 두는게 나을 듯
                .addFilterBefore(jwtFilter, SecurityContextHolderFilter.class);

        return http.build();
    }
}