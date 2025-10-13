package com.example.demo.domain.mail.enums;

import com.example.demo.domain.mail.exception.InvalidEmailAuthPurposeException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;


@Getter
public enum EmailAuthPurpose {
    SIGN_UP("회원가입"),
    PASSWORD_RESET("비밀번호 초기화");

    private final String value;

    EmailAuthPurpose(String value) {
        this.value = value;
    }

    //  @JsonCreator: 문자열을 Enum으로 변환하는 메서드
    // Spring은 이 메서드를 보고 "SIGN_UP"이라는 문자열을 EmailAuthPurpose.SIGN_UP 객체로 변환
    @JsonCreator
    public static EmailAuthPurpose fromValue(String value) {
        return Stream.of(EmailAuthPurpose.values())
                .filter(purpose -> purpose.name().equalsIgnoreCase(value)) // name()과 비교 (SIGN_UP, PASSWORD_RESET)
                .findFirst()
                .orElseThrow(() -> new InvalidEmailAuthPurposeException(value + "는 유효하지 않은 인증 목적입니다."));
    }

    //  @JsonValue: Enum을 문자열로 변환하는 메서드
    // 이 Enum 객체를 JSON으로 만들 때 어떤 값을 사용할지 지정
    @JsonValue
    public String getKey() {
        return name(); // "SIGN_UP", "PASSWORD_RESET"
    }

}

