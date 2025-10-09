package com.example.demo.global.annotation.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Swagger 문서에 401 Unauthorized 응답을 공통으로 추가합니다.
 * (로그인이 필요한 API에 사용)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiUnauthorizedResponse {
}
