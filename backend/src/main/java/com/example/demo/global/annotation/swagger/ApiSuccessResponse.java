package com.example.demo.global.annotation.swagger;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiSuccessResponse {
    String description();
    String message() default "요청이 성공했습니다";
    String responseCode() default "200";
    Class<?> dataType() default Void.class;
}