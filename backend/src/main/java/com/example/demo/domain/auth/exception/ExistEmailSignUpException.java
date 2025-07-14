package com.example.demo.domain.auth.exception;

import com.example.demo.global.exception.AuthException;

public class ExistEmailSignUpException extends AuthException {
  public ExistEmailSignUpException() {
    super("이미 회원가입 되어 있는 이메일 입니다.","409");
  }
}
