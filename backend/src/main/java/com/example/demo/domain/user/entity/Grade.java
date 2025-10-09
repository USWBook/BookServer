package com.example.demo.domain.user.entity;

import com.example.demo.domain.user.exception.InvalidGradeException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum Grade {
    GRADE_1(1),
    GRADE_2(2),
    GRADE_3(3),
    GRADE_4(4),
    GRADE_5(5);

    private final int value;

    Grade(int value) {
        this.value = value;
    }

    // JSON 요청의 숫자 값을 Enum으로 변환할 때 사용
    @JsonCreator
    public static Grade fromValue(int value) {
        return Stream.of(Grade.values())
                .filter(g -> g.getValue() == value)
                .findFirst()
                .orElseThrow(() -> new InvalidGradeException(value + "는 유효하지 않은 학년입니다."));
    }

    // Enum을 JSON으로 변환할 때 숫자 값으로 나가도록 설정
    @JsonValue
    public int getValue() {
        return value;
    }
}
