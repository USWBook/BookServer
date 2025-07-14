package com.example.demo.domain.auth.controller;

import com.example.demo.domain.auth.dto.request.LoginRequest;
import com.example.demo.domain.auth.dto.request.TokenResponse;
import com.example.demo.domain.auth.exception.InvalidPasswordException;
import com.example.demo.domain.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("로그인 성공 시 accessToken과 refreshToken을 반환한다")
    void login_success() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        TokenResponse response = new TokenResponse("access-token", "refresh-token");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("비밀번호 틀리면 401 응답")
    void login_invalidPassword() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "wrong-password");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidPasswordException());

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("비밀번호가 잘못되었습니다."));
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 로그아웃 시 401 Unauthorized 반환")
    void logout_fail_invalidToken() throws Exception {
        // given
        String invalidJwt = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.invalidsignature";


        // JwtAuthenticationFilter 등에서 예외 발생한다고 가정
        // 실제 필터는 서비스 메서드 전에 작동하므로, 여기선 Controller 레벨로 기대값 확인만

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", invalidJwt))
                .andExpect(status().isUnauthorized());
    }
}