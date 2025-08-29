package com.example.demo.domain.mail.service;

import com.example.demo.domain.mail.service.MailService;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @InjectMocks
    private MailService mailService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private RedisTokenRepository redisTokenRepository;

    @Test
    @DisplayName("인증 메일 발송 및 인증 코드 Redis 저장 테스트")
    void sendVerificationMail() {
        // given
        String email = "test@suwon.ac.kr";

        // 반환값이 없(void)으므로, 어떤 일이 발생하지 않도록(doNothing) 설정
        doNothing().when(redisTokenRepository).saveVerificationCode(anyString(), anyString(), anyLong());
        doNothing().when(mailSender).send(any(jakarta.mail.internet.MimeMessage.class));

        // when
        mailService.sendVerificationCode(email);

        // then
        // 1. Redis 저장 메서드가 올바른 이메일과 함께 호출되었는지 검증
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisTokenRepository).saveVerificationCode(emailCaptor.capture(), codeCaptor.capture(), anyLong());

        assertThat(emailCaptor.getValue()).isEqualTo(email);
        assertThat(codeCaptor.getValue()).isNotNull(); // 인증 코드가 생성되었는지
        assertThat(codeCaptor.getValue().length()).isEqualTo(6); // 6자리 숫자인지

        // 2. 메일 전송 메서드가 1번 호출되었는지 검증
        verify(mailSender).send(any(jakarta.mail.internet.MimeMessage.class));
    }
}