package com.example.demo.global.config.cors;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Optional;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
@RequiredArgsConstructor
public class CorsConfig {

    private final CorsProperties corsProps;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // 정확 매칭: allowedOrigins / 패턴 매칭: allowedOriginPatterns
        Optional.ofNullable(corsProps.allowedOrigins()).ifPresent(cfg::setAllowedOrigins);
        Optional.ofNullable(corsProps.allowedOriginPatterns()).ifPresent(cfg::setAllowedOriginPatterns);

        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With","Accept","Origin"));

        // ✅ 브라우저 JS에서 읽히도록
        cfg.setExposedHeaders(List.of("Authorization","Content-Disposition"));

        // 쿠키/자격증명 전송 허용 (SameSite=None 쿠키 등 사용 시 필수)
        cfg.setAllowCredentials(true);

        // 적용 경로
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
