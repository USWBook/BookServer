package com.example.demo.global.annotation.swagger;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiSuccessResponse {
    String description() default "성공";
    // data 필드의 DTO 클래스를 지정, 기본값은 'data 없음'을 의미하는 Void.class
    Class<?> dataType() default Void.class;
}