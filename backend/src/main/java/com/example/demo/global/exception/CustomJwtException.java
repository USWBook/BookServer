package com.example.demo.global.exception;

import io.jsonwebtoken.JwtException;
import lombok.Getter;

@Getter
public class CustomJwtException extends JwtException {
    private final String code;

    public CustomJwtException(String message, String code) {
        super(message);
        this.code = code;
    }
    public int getStatusCode() { return Integer.parseInt(code);}
}
