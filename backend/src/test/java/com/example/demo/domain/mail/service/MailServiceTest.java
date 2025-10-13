package com.example.demo.domain.mail.service;

import com.example.demo.domain.mail.dto.MailRequestOrVerifyDto;
import com.example.demo.domain.mail.enums.EmailAuthPurpose;
import com.example.demo.domain.mail.enums.MailStatus;
import com.example.demo.domain.mail.exception.InvalidEmailAuthPurposeException;
import com.example.demo.global.redis.repository.RedisMailRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.demo.domain.mail.exception.InvalidOrExpiredVerificationCodeException;
import org.junit.jupiter.api.function.Executable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
    private RedisMailRepository redisMailRepository;


    @Test
    @DisplayName("인증코드 검증 성공")
    void verifyEmail_Success(){
        // given
        String email = "test@suwon.ac.kr";
        String correctCode = "123456";

        // 1. 해당 이메일로 인증 요청이 있었다고 설정
        given(redisMailRepository.existsVerificationCode(email, EmailAuthPurpose.SIGN_UP)).willReturn(true);
        // 2. Redis에 저장된 코드가 입력된 코드와 일치한다고 설정
        given(redisMailRepository.getVerificationCode(email,EmailAuthPurpose.SIGN_UP)).willReturn(Optional.of(correctCode));

        // when
        // 예외가 발생하지 않아야 함
        Executable executable = () -> mailService.verifyEmail(email, correctCode,EmailAuthPurpose.SIGN_UP);

        // then
        assertDoesNotThrow(executable);

        // 인증 성공 후, 이메일이 '인증됨'으로 저장되고 기존 인증 코드는 삭제되는지 행위를 검증
        verify(redisMailRepository).setVerifiedEmail(email,EmailAuthPurpose.SIGN_UP);
        verify(redisMailRepository).deleteVerificationCode(email,EmailAuthPurpose.SIGN_UP);
    }

    @Test
    @DisplayName("인증코드 검증 실패 - 인증 요청을 보낸 적 없음")
    void verifyEmail_Fail_CodeNotRequested() {
        // given
        String email = "test@suwon.ac.kr";
        String anyCode = "123456";

        // 1. 해당 이메일로 인증 요청을 한 기록이 없다고 설정
        given(redisMailRepository.existsVerificationCode(email,EmailAuthPurpose.SIGN_UP)).willReturn(false);

        // when & then
        // VerificationCodeNotRequestedException 예외가 발생하는지 검증
        assertThatThrownBy(() -> mailService.verifyEmail(email, anyCode,EmailAuthPurpose.SIGN_UP))
                .isInstanceOf(InvalidEmailAuthPurposeException.class);

        // 후속 메서드들이 절대 호출되지 않았는지 검증
        verify(redisMailRepository, never()).getVerificationCode(anyString(),EmailAuthPurpose.SIGN_UP);
        verify(redisMailRepository, never()).setVerifiedEmail(anyString(),EmailAuthPurpose.SIGN_UP);
        verify(redisMailRepository, never()).deleteVerificationCode(anyString(),EmailAuthPurpose.SIGN_UP);
    }

    @Test
    @DisplayName("인증코드 검증 실패 - 코드 불일치")
    void verifyEmail_Fail_CodeMismatch() {
        // given
        String email = "test@suwon.ac.kr";
        String incorrectCode = "654321"; // 사용자가 입력한 틀린 코드
        String savedCode = "123456";    // Redis에 저장된 올바른 코드

        // 1. 인증 요청 기록은 있다고 설정
        given(redisMailRepository.existsVerificationCode(email,EmailAuthPurpose.SIGN_UP)).willReturn(true);
        // 2. Redis에서 가져온 코드가 사용자가 입력한 코드와 다르다고 설정
        given(redisMailRepository.getVerificationCode(email,EmailAuthPurpose.SIGN_UP)).willReturn(Optional.of(savedCode));

        // when & then
        // InvalidOrExpiredVerificationCodeException 예외가 발생하는지 검증
        assertThatThrownBy(() -> mailService.verifyEmail(email, incorrectCode,EmailAuthPurpose.SIGN_UP))
                .isInstanceOf(InvalidOrExpiredVerificationCodeException.class);

        // 인증 성공 로직이 절대 호출되지 않았는지 검증
        verify(redisMailRepository, never()).setVerifiedEmail(anyString(),EmailAuthPurpose.SIGN_UP);
        verify(redisMailRepository, never()).deleteVerificationCode(anyString(),EmailAuthPurpose.SIGN_UP);
    }

    @Test
    @DisplayName("인증 메일 발송 및 인증 코드 Redis 저장 테스트")
    void sendVerificationMail() throws Exception {
        // given
        String email = "test@suwon.ac.kr";
        MailRequestOrVerifyDto mailRequestOrVerifyDto = new MailRequestOrVerifyDto(email, EmailAuthPurpose.SIGN_UP);

        doNothing().when(redisMailRepository).saveVerificationCode(anyString(), anyString(), anyLong(), eq(EmailAuthPurpose.SIGN_UP));
        given(emailSendingService.sendAuthCodeEmail(anyString(), anyString(), eq(EmailAuthPurpose.SIGN_UP.getValue())))
                .willReturn(CompletableFuture.completedFuture(null));

        // when
        mailService.sendVerificationCode(mailRequestOrVerifyDto);

        // then
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        verify(redisMailRepository).saveVerificationCode(emailCaptor.capture(), codeCaptor.capture(), anyLong(), eq(EmailAuthPurpose.SIGN_UP));

        assertThat(emailCaptor.getValue()).isEqualTo(email);
        assertThat(codeCaptor.getValue()).isNotNull();
        assertThat(codeCaptor.getValue().length()).isEqualTo(6);

        verify(emailSendingService).sendAuthCodeEmail(anyString(), anyString(), eq(EmailAuthPurpose.SIGN_UP.getValue()));
    }

    @Test
    @DisplayName("메일 전송 상태 확인 - 요청 목적이 다르면 상태가 조회되지 않음")
    void getMailStatus_Fail_WhenPurposeMismatches() {
        // given
        String email = "test@suwon.ac.kr";

        // "SIGN_UP" 목적으로 요청했을 때의 상태는 "SUCCESS"라고 가정
        given(redisMailRepository.getMailStatus(email, EmailAuthPurpose.SIGN_UP))
                .willReturn(Optional.of(MailStatus.SUCCESS));

        // "PASSWORD_RESET" 목적으로 요청했을 때는 Redis에 데이터가 없으므로 Optional.empty()를 반환한다고 가정
        given(redisMailRepository.getMailStatus(email, EmailAuthPurpose.PASSWORD_RESET))
                .willReturn(Optional.empty());

        // when
        // SIGN_UP 목적으로 상태 조회
        Optional<MailStatus> signUpStatus = mailService.getMailStatus(email, EmailAuthPurpose.SIGN_UP);

        // PASSWORD_RESET 목적으로 상태 조회
        Optional<MailStatus> passwordResetStatus = mailService.getMailStatus(email, EmailAuthPurpose.PASSWORD_RESET);

        // then
        // SIGN_UP 조회 결과는 SUCCESS가 포함된 Optional이어야 함
        assertThat(signUpStatus).isPresent();
        assertThat(signUpStatus.get()).isEqualTo(MailStatus.SUCCESS);

        // PASSWORD_RESET 조회 결과는 비어있는 Optional이어야 함
        assertThat(passwordResetStatus).isNotPresent();
    }
}