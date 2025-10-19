package com.example.demo.domain.user.service;

import com.example.demo.domain.auth.exception.InvalidPasswordException;
import com.example.demo.domain.auth.exception.UserNotFoundException;
import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.user.dto.ChangeInfoRequest;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.enums.Grade;
import com.example.demo.domain.user.enums.Semester;
import com.example.demo.domain.user.enums.UserStatus;
import com.example.demo.domain.user.exception.PasswordNotEqualException;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.response.UserInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MajorRepository majorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UUID majorId;
    private User user;
    private UUID userId;
    private String userName;
    private Major major;
    private String password;
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        majorId = UUID.randomUUID();
        userName = "테스트유저";
        password = "password123";

        major = Major.builder()
                .id(majorId)
                .name("테스트학과")
                .build();

        user = User.builder()
                .id(userId)
                .password("encodedPassword123")
                .name(userName)
                .major(major)
                .grade(Grade.GRADE_1)
                .semester(Semester.SEMESTER_1)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("사용자 정보 조회 성공")
    void getUserInfo_Success() {
        // given
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        UserInfoResponse response = userService.getUserInfo(userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo(userName);
    }

    @Test
    void changeInformation() {
        // given
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        Major major = Major.builder().id(majorId).name("테스트학과").build();
        given(majorRepository.findById(majorId)).willReturn(Optional.of(major));

        ChangeInfoRequest request = new ChangeInfoRequest(
                "바뀐이름",
                majorId,
                Grade.GRADE_1,
                Semester.SEMESTER_1
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(majorRepository.findById(majorId)).willReturn(Optional.of(major));

        // when
        UserInfoResponse response = userService.changeInformation(userId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("바뀐이름");
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void withdraw_Success() {
        // given
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(password, "encodedPassword123")).willReturn(true);

        // when
        userService.withdraw(userId, password);

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWAL);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 비밀번호 불일치")
    void withdraw_Fail_InvalidPassword() {
        // given
        String wrongPassword = "wrong-password";
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        given(passwordEncoder.matches(wrongPassword, "encodedPassword123")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.withdraw(userId, wrongPassword))
                .isInstanceOf(PasswordNotEqualException.class);

        assertThat(user.getStatus()).isNotEqualTo(UserStatus.WITHDRAWAL);
    }

    @Test
    void banUser() {
    }
}