package com.example.demo.domain.post.enums;

import com.example.demo.domain.post.exception.InvalidPostStatusException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.stream.Stream;

public enum PostStatus {
    SELLING("판매중"),
    SOLD("판매완료");

    private final String value;

    PostStatus(String value) {this.value = value;}
    

    @JsonCreator
    public static com.example.demo.domain.post.enums.PostStatus fromValue(String value) {
        return Stream.of(com.example.demo.domain.post.enums.PostStatus.values())
                .filter(g -> Objects.equals(g.getValue(), value))
                .findFirst()
                .orElseThrow(() -> new InvalidPostStatusException(value + "는 유효하지 않은 상태입니다."));
    }

    // Enum을 JSON으로 변환할 때 String 값으로 나가도록 설정
    @JsonValue
    public String getValue() {
        return value;
    }
}
