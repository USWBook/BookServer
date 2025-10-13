package com.example.demo.global.annotation.enums;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//  이 어노테이션의 유효성 검증을 EnumValidator 클래스가 처리하도록 지정
@Constraint(validatedBy = EnumValidator.class)
//  어노테이션을 필드, 메서드, 파라미터에 사용할 수 있도록 설정
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
// 런타임까지 어노테이션 정보를 유지
@Retention(RetentionPolicy.RUNTIME)
public @interface Enum {
    String message() default "유효하지 않은 값입니다."; // 유효성 검증 실패 시 기본 메시지
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    Class<? extends java.lang.Enum<?>> enumClass(); //  검증할 Enum 클래스를 지정받는 속성
    boolean ignoreCase() default false; //  대소문자 구분 여부
}