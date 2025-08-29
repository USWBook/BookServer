package com.example.demo.global.jwt.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.example.demo.global.util.Ut.Json.objectMapper;

public class JwtAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        String errorCode;
        String errorMessage;

        //  instanceof를 사용해 예외 종류를 확인하고, 그에 맞는 메시지를 설정
        if (exception instanceof UsernameNotFoundException) {
            errorCode = "404";
            errorMessage = "존재하지 않는 계정입니다.";
        } else if (exception instanceof BadCredentialsException) {
            errorCode = "400";
            errorMessage = "비밀번호가 일치하지 않습니다.";
        } else {
            // 그 외 다른 모든 인증 관련 예외 처리
            errorCode = "400";
            errorMessage = "인증에 실패하였습니다. 관리자에게 문의하세요.";
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 상태 코드
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Map을 사용하여 JSON 응답 본문을 생성
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("code", errorCode);
        errorDetails.put("message", errorMessage);

        // ObjectMapper를 사용해 Map을 JSON 문자열로 변환하고 응답
        objectMapper.writeValue(response.getWriter(), errorDetails);
    }
}

