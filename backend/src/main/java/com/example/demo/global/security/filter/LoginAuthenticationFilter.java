package com.example.demo.global.security.filter;

import com.example.demo.domain.auth.dto.request.LoginRequest;
import com.example.demo.domain.auth.exception.BannedUserException;
import com.example.demo.domain.user.entity.UserStatus;
import com.example.demo.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

//@Component
@RequiredArgsConstructor
@Slf4j
public class LoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

//    public LoginAuthenticationFilter(AuthenticationManager authenticationManager) {
//        super.setAuthenticationManager(authenticationManager);
//        // 기본 처리 URL은 SecurityConfig에서 setFilterProcessesUrl로 바꿔줘도 됨
//    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            throw new AuthenticationServiceException("허용되지 않은 요청 방식입니다. POST 메서드를 사용해 주십시오.");
        }

        try (ServletInputStream is = request.getInputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            LoginRequest loginRequest = objectMapper.readValue(is, LoginRequest.class);

            String email = loginRequest.email();
            userRepository.findByEmail(email).ifPresent(user -> {
                if (user.getStatus() == UserStatus.BANNED) {
                    throw new BannedUserException();
                }
            });

            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());

            setDetails(request, authRequest);

            return authenticationManager.authenticate(authRequest);
        } catch (IOException e) {
            throw new AuthenticationServiceException("인증 요청 본문을 처리하는 데 실패했습니다.", e);
        }
    }
}

