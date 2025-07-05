package com.example.demo.global.exception;

import lombok.Getter;

@Getter
public class BookException extends RuntimeException {
    private final String code;
    public BookException(String code, String message) {
        super(message);
        this.code = code;
    }
    public int getStatusCode() { return Integer.parseInt(code);}
}
