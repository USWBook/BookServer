package com.example.demo.global.annotation.enums;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<Enum, String> {

    private Class<? extends java.lang.Enum<?>> enumClass;
    private boolean ignoreCase; //false(기본값) 일경우 대소문자도 따짐

    @Override
    public void initialize(Enum annotation) {
        // @Enum 어노테이션에서 정의한 속성들을 가져와 초기화
        this.enumClass = annotation.enumClass();
        this.ignoreCase = annotation.ignoreCase();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // @NotBlank 등으로 null 체크는 따로 처리하는 것이 좋음
        }

        // Enum에 포함된 모든 상수들을 순회하며 입력값과 비교
        return java.util.Arrays.stream(enumClass.getEnumConstants()) //  Enum의 모든 상수를 가져와서
                .anyMatch(enumConstant -> { //  그 중 하나라도(anyMatch) 아래 조건을 만족하는지 확인
                    if (ignoreCase) {
                        return enumConstant.name().equalsIgnoreCase(value);
                    } else {
                        return enumConstant.name().equals(value);
                    }
                }); //  만족하는 상수가 '하나도 없으면' 최종적으로 false를 반환
    }
}
