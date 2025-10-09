package com.example.demo.global.annotation.swagger;

import java.lang.annotation.*;

/**
 * Swagger 문서에 403 Forbidden 응답을 공통으로 추가합니다.
 * (특정 권한이 필요한 API에 사용)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiForbiddenResponse {
}