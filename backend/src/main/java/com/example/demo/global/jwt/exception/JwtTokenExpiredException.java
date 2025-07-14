package com.example.demo.global.jwt.exception;

import com.example.demo.global.exception.CustomJwtException;

// 토큰 만료 예외
public class JwtTokenExpiredException extends CustomJwtException {
    public JwtTokenExpiredException() {
        super("토큰이 만료되었습니다.", "401");
    }
}
