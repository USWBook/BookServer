package com.example.demo.domain.post.exception;

import com.example.demo.global.exception.EnumException;

public class InvalidPostStatusException extends EnumException {
  public InvalidPostStatusException(String message) {
    super(message , "400" );
  }
}