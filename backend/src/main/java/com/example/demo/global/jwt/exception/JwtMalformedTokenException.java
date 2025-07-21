package com.example.demo.global.jwt.exception;

import com.example.demo.global.exception.CustomJwtException;

// 형식 오류
public class JwtMalformedTokenException extends CustomJwtException {
    public JwtMalformedTokenException() {
        super("토큰 형식이 잘못되었습니다.", "400");
    }
}
