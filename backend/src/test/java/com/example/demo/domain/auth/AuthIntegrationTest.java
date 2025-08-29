package com.example.demo.domain.auth;

import com.example.demo.domain.auth.dto.request.LoginRequest;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.entity.UserStatus;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.role.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest // 스프링 컨텍스트를 모두 로드하여 테스트
@AutoConfigureMockMvc // MockMvc를 DI 받기 위한 어노테이션
@Transactional // 각 테스트 후 롤백하여 DB 상태를 유지
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String LOGIN_URL = "/api/auth/login";

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 미리 저장
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@suwon.ac.kr")
                .password(passwordEncoder.encode("password123")) // 비밀번호는 반드시 암호화하여 저장
                .name("테스트유저")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);
    }

    @Test
    @DisplayName("로그인 통합 테스트 - 성공")
    void login_Integration_Success() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest("test@suwon.ac.kr", "password123");
        String requestBody = objectMapper.writeValueAsString(loginRequest);

        // when
        ResultActions resultActions = mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists()) // accessToken이 존재하는지
                .andExpect(cookie().exists("refreshToken")) // refreshToken 쿠키가 존재하는지
                .andExpect(cookie().httpOnly("refreshToken", true)); // HttpOnly 속성 확인
    }

    @Test
    @DisplayName("로그인 통합 테스트 - 실패 (비밀번호 오류)")
    void login_Integration_Fail_WrongPassword() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest("test@suwon.ac.kr", "wrong-password");
        String requestBody = objectMapper.writeValueAsString(loginRequest);

        // when
        ResultActions resultActions = mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        // JwtAuthenticationFailureHandler가 401 Unauthorized를 반환하도록 설정되어 있어야 함
        resultActions.andExpect(status().isUnauthorized());
    }
}