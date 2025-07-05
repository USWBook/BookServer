package com.example.demo.domain.major.exception;

import com.example.demo.global.exception.BookException;

public class MajorNotFoundException extends BookException {
    public MajorNotFoundException() {
        super("404", "존재하지 않는 전공입니다.");
    }
}