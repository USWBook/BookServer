package com.example.demo.domain.auth.controller;

import com.example.demo.domain.auth.controller.AuthController;
import com.example.demo.domain.auth.dto.request.SignUpRequest;
import com.example.demo.domain.auth.service.AuthService;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.global.exception.GlobalExceptionHandler;
import com.example.demo.global.jwt.JwtAuthenticationFilter;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.jwt.service.TokenService;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import com.example.demo.global.security.SecurityConfig;
import com.example.demo.global.security.handler.CustomAccessDeniedHandler;
import com.example.demo.global.security.handler.CustomAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("회원가입 API 성공 테스트")
    void signUpApiSuccess() throws Exception {
        // ... (Test implementation is correct)
        SignUpRequest request = new SignUpRequest(
                "test@suwon.ac.kr", "ValidPassword1!", "Test User",
                "20240001", "Software Engineering", 2, 1
        );
        doNothing().when(authService).signUp(any(SignUpRequest.class));
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다.")) // Adjust message if needed
                .andDo(print());

        verify(authService).signUp(any(SignUpRequest.class));
    }

    @Test
    @DisplayName("회원가입 API 실패 테스트 - 유효하지 않은 이메일")
    void signUpApiFail_InvalidEmail() throws Exception {
        // ... (Test implementation is correct)
        SignUpRequest request = new SignUpRequest(
                "invalid-email", "ValidPassword1!", "Test User",
                "20240001", "Software Engineering", 2, 1
        );
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andDo(print());
    }
}