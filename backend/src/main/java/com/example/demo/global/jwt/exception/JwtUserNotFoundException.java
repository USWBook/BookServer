package com.example.demo.global.jwt.exception;

import com.example.demo.global.exception.CustomJwtException;

// 토큰은 유효하지만 DB에 사용자가 없는 경우
public class JwtUserNotFoundException extends CustomJwtException {
    public JwtUserNotFoundException() {
        super("토큰에 해당하는 사용자를 찾을 수 없습니다.", "401");
    }
}
