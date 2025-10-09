package com.example.demo.domain.user.entity;

import com.example.demo.domain.user.exception.InvalidSemesterException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum Semester {
    SEMESTER_1(1),
    SEMESTER_2(2);

    private final int value;

    Semester(int value) {
        this.value = value;
    }

    // JSON 요청의 숫자 값을 Enum으로 변환할 때 사용
    @JsonCreator
    public static Semester fromValue(int value) {
        return Stream.of(Semester.values())
                .filter(g -> g.getValue() == value)
                .findFirst()
                .orElseThrow(() -> new InvalidSemesterException(value + "는 유효하지 않은 학기입니다."));
    }

    // Enum을 JSON으로 변환할 때 숫자 값으로 나가도록 설정
    @JsonValue
    public int getValue() {
        return value;
    }
}
