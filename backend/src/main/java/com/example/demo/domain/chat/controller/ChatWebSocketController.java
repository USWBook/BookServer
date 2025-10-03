package com.example.demo.domain.chat.controller;

import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
import com.example.demo.domain.chat.dto.response.SendMessageResponseDto;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.service.ChatService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.dto.CustomUserDetails; // CustomUserDetails import
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.Payload;
import java.util.UUID; // UUID import 추가

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    @MessageMapping("/chat.send")
    // @AuthenticationPrincipal CustomUserDetails 사용
    public void sendMessage(@Payload SendMessageRequestDto messageDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        if (customUserDetails == null) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        // 🚨 수정된 부분: customUserDetails의 getId()와 getEmail()을 바로 사용
        UUID senderId = customUserDetails.getId();
        String senderEmail = customUserDetails.getEmail();

        log.info("메시지 수신: roomId={}, senderId={}, senderEmail={}, message={}",
                messageDto.roomId(), senderId, senderEmail, messageDto.message());

        // 메시지 저장 시 ID만 사용 (DB 재조회 불필요)
        // ChatService의 sendChatMessage 메서드가 User ID(UUID)를 받도록 설계되어 있어야 합니다.
        ChatMessage savedMessage = chatService.sendChatMessage(messageDto, senderId); // ⬅️ ID 사용

        // 응답 DTO 생성
        SendMessageResponseDto.Data data = new SendMessageResponseDto.Data(
                savedMessage.getId(),
                savedMessage.getChatRoomId(),
                savedMessage.getSender().getId(),
                savedMessage.getContent(),
                savedMessage.getSentAt()
        );

        // 구독 중인 클라이언트에게 전송
        String destination = "/sub/chat/" + savedMessage.getChatRoomId();
        messagingTemplate.convertAndSend(destination, data);
    }
}