package com.example.demo.domain.mail.service;

import com.example.demo.global.redis.repository.RedisTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import com.example.demo.domain.mail.exception.InvalidOrExpiredVerificationCodeException;
import com.example.demo.domain.mail.exception.VerificationCodeNotRequestedException;
import org.junit.jupiter.api.function.Executable;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @InjectMocks
    private MailService mailService;

    @Mock
    private EmailSendingService emailSendingService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private RedisTokenRepository redisTokenRepository;


    @Test
    @DisplayName("인증코드 검증 성공")
    void verifyEmail_Success(){
        // given
        String email = "test@suwon.ac.kr";
        String correctCode = "123456";

        // 1. 해당 이메일로 인증 요청이 있었다고 설정
        given(redisTokenRepository.existsVerificationCode(email)).willReturn(true);
        // 2. Redis에 저장된 코드가 입력된 코드와 일치한다고 설정
        given(redisTokenRepository.getVerificationCode(email)).willReturn(correctCode);

        // when
        // 예외가 발생하지 않아야 함
        Executable executable = () -> mailService.verifyEmail(email, correctCode);

        // then
        assertDoesNotThrow(executable);

        // 인증 성공 후, 이메일이 '인증됨'으로 저장되고 기존 인증 코드는 삭제되는지 행위를 검증
        verify(redisTokenRepository).setVerifiedEmail(email);
        verify(redisTokenRepository).deleteVerificationCode(email);
    }

    @Test
    @DisplayName("인증코드 검증 실패 - 인증 요청을 보낸 적 없음")
    void verifyEmail_Fail_CodeNotRequested() {
        // given
        String email = "test@suwon.ac.kr";
        String anyCode = "123456";

        // 1. 해당 이메일로 인증 요청을 한 기록이 없다고 설정
        given(redisTokenRepository.existsVerificationCode(email)).willReturn(false);

        // when & then
        // VerificationCodeNotRequestedException 예외가 발생하는지 검증
        assertThatThrownBy(() -> mailService.verifyEmail(email, anyCode))
                .isInstanceOf(VerificationCodeNotRequestedException.class);

        // 후속 메서드들이 절대 호출되지 않았는지 검증
        verify(redisTokenRepository, never()).getVerificationCode(anyString());
        verify(redisTokenRepository, never()).setVerifiedEmail(anyString());
        verify(redisTokenRepository, never()).deleteVerificationCode(anyString());
    }

    @Test
    @DisplayName("인증코드 검증 실패 - 코드 불일치")
    void verifyEmail_Fail_CodeMismatch() {
        // given
        String email = "test@suwon.ac.kr";
        String incorrectCode = "654321"; // 사용자가 입력한 틀린 코드
        String savedCode = "123456";    // Redis에 저장된 올바른 코드

        // 1. 인증 요청 기록은 있다고 설정
        given(redisTokenRepository.existsVerificationCode(email)).willReturn(true);
        // 2. Redis에서 가져온 코드가 사용자가 입력한 코드와 다르다고 설정
        given(redisTokenRepository.getVerificationCode(email)).willReturn(savedCode);

        // when & then
        // InvalidOrExpiredVerificationCodeException 예외가 발생하는지 검증
        assertThatThrownBy(() -> mailService.verifyEmail(email, incorrectCode))
                .isInstanceOf(InvalidOrExpiredVerificationCodeException.class);

        // 인증 성공 로직이 절대 호출되지 않았는지 검증
        verify(redisTokenRepository, never()).setVerifiedEmail(anyString());
        verify(redisTokenRepository, never()).deleteVerificationCode(anyString());
    }


    @Test
    @DisplayName("인증 메일 발송 및 인증 코드 Redis 저장 테스트")
    void sendVerificationMail() throws Exception {
        // given
        String email = "test@suwon.ac.kr";

        doNothing().when(redisTokenRepository).saveVerificationCode(anyString(), anyString(), anyLong());
        doNothing().when(emailSendingService).sendAuthCodeEmail(anyString(), anyString());

        // when
        mailService.sendVerificationCode(email);

        // then
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisTokenRepository).saveVerificationCode(emailCaptor.capture(), codeCaptor.capture(), anyLong());

        assertThat(emailCaptor.getValue()).isEqualTo(email);
        assertThat(codeCaptor.getValue()).isNotNull();
        assertThat(codeCaptor.getValue().length()).isEqualTo(6);

        verify(emailSendingService).sendAuthCodeEmail(anyString(), anyString());
    }
}