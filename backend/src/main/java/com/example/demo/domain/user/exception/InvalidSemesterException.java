package com.example.demo.domain.user.exception;

import com.example.demo.global.exception.EnumException;

public class InvalidSemesterException extends EnumException {
  public InvalidSemesterException(String message) {
    super(message , "400" );
  }
}
