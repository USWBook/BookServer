package com.example.demo.domain.report.enums;

import com.example.demo.domain.post.exception.InvalidPostStatusException;
import com.example.demo.global.exception.EnumException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.stream.Stream;

public enum ReportType {
    POST("post"),
    CHAT("chat");

    private final String value;

    ReportType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static com.example.demo.domain.report.enums.ReportType fromValue(String value) {
        return Stream.of(com.example.demo.domain.report.enums.ReportType.values())
                .filter(g -> Objects.equals(g.getValue(), value))
                .findFirst()
                .orElseThrow(() -> new EnumException(value + "는 유효하지 않은 신고대상입니다.","400"));
    }

    // Enum을 JSON으로 변환할 때 String 값으로 나가도록 설정
    @JsonValue
    public String getValue() {
        return value;
    }
}
