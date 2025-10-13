package com.example.demo.global.annotation.enums;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<Enum, String> {

    private Class<? extends java.lang.Enum<?>> enumClass;
    private boolean ignoreCase;

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
        return java.util.Arrays.stream(enumClass.getEnumConstants())
                .anyMatch(enumConstant -> {
                    if (ignoreCase) {
                        return enumConstant.name().equalsIgnoreCase(value);
                    } else {
                        return enumConstant.name().equals(value);
                    }
                });
    }
}
