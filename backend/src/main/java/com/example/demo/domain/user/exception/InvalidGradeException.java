package com.example.demo.domain.user.exception;

import com.example.demo.global.exception.EnumException;

public class InvalidGradeException extends EnumException {
    public InvalidGradeException(String message) {
        super(message , "400" );
    }
}
