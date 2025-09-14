//package com.example.demo.global.config.cors;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.List;
//import java.util.Optional;
//
//@Configuration
//@EnableConfigurationProperties(CorsProperties.class)
//@RequiredArgsConstructor
//public class CorsConfig {
//
//    private final CorsProperties corsProps;
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration cfg = new CorsConfiguration();
//
//        Optional.ofNullable(corsProps.allowedOrigins()).ifPresent(cfg::setAllowedOrigins);
//        Optional.ofNullable(corsProps.allowedOriginPatterns()).ifPresent(cfg::setAllowedOriginPatterns);
//
//        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
//        cfg.setAllowedHeaders(List.of("*"));
//        cfg.setExposedHeaders(List.of("*"));
//        cfg.setAllowCredentials(true); // 쿠키/자격증명 사용 시 true
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", cfg);
//        return source;
//    }
//}