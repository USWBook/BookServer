package com.example.demo.global.exception;

import io.jsonwebtoken.JwtException;
import lombok.Getter;

// runtimeException이면 필터에서 못잡고 500 에러가 뜨며 컨트롤러단까지 예외가 전파되서 코드 구조 노출이 됨
@Getter
public class CustomJwtException extends JwtException {
    private final String code;

    public CustomJwtException(String message, String code) {
        super(message);
        this.code = code;
    }
    public int getStatusCode() { return Integer.parseInt(code);}
}
