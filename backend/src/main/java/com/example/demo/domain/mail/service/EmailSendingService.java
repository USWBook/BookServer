package com.example.demo.domain.mail.service;

import com.example.demo.domain.mail.exception.MessagingFailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSendingService {

    private final JavaMailSender javaMailSender;

    // 인증 코드를 이메일로 발송
    public void sendAuthCodeEmail(String email, String authCode) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("[USWBook] 이메일 인증을 위한 인증 코드 발송");

            // HTML 형식의 이메일 본문
            String htmlContent = "<p>USWBook 가입해주셔서 감사합니다.</p>"
                    + "<p>인증을 완료하려면 아래의 6자리 코드를 입력해주세요.(유효기간은 30분입니다)</p>"
                    + "<h2>" + authCode + "</h2>";
            mimeMessageHelper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new MessagingFailException(e.getMessage());
        }
    }
}
