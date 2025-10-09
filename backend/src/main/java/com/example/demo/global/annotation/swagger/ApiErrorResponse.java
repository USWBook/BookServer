package com.example.demo.global.annotation.swagger;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ApiErrorResponses.class) // 반복 사용 설정은 그대로 유지
public @interface ApiErrorResponse {
    String responseCode() default "400";
    String description();
    String exampleName();
    String exampleValue();
}