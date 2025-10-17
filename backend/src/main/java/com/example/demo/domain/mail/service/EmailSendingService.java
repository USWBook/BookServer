package com.example.demo.domain.mail.service;

import com.example.demo.domain.mail.enums.EmailAuthPurpose;
import com.example.demo.domain.mail.exception.MessagingFailException;
import com.example.demo.global.redis.repository.RedisMailRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class EmailSendingService {

    private final JavaMailSender javaMailSender;
    private final RedisMailRepository redisMailRepository;

    @Async("EmailThreadPoolTaskExecutor")
    // 인증 코드를 이메일로 발송
    public CompletableFuture<Void> sendAuthCodeEmail(String email, String authCode, String purposeValue) throws Exception {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("[USWBook] " + purposeValue + "을(를) 위한 인증 코드 발송");

        String htmlContent = "<p>USWBook " + purposeValue + " 인증코드 입니다.</p>"
                + "<p>인증을 완료하려면 아래의 6자리 코드를 입력해주세요.(유효기간은 30분입니다)</p>"
                + "<h2>" + authCode + "</h2>";
        mimeMessageHelper.setText(htmlContent, true);

        javaMailSender.send(mimeMessage);

        return CompletableFuture.completedFuture(null);
    }
}
