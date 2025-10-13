package com.example.demo.domain.mail.exception;

public class TooManyMailRequestException extends RuntimeException {
    public TooManyMailRequestException() {
        super("인증 메일 요청은 1분에 한 번만 가능합니다.");
    }
}
