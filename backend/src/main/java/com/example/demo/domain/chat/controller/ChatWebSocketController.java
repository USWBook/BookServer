package com.example.demo.domain.chat.controller;

import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
import com.example.demo.domain.chat.dto.response.SendMessageResponseDto;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.service.ChatService;
import com.example.demo.domain.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import com.example.demo.global.jwt.JwtProvider;

import java.util.UUID;

@Controller
public class ChatWebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final JwtProvider jwtProvider;
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate, ChatService chatService, JwtProvider jwtProvider) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.jwtProvider = jwtProvider;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(SendMessageRequestDto messageDto,
                            @Header("Authorization") String token) {

        // STOMP CONNECT frame에서 전달된 Authorization 토큰 확인
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized");
        }

        // "Bearer " 제거
        String jwt = token.substring(7);

        // JWT에서 이메일 추출
        String email = jwtProvider.extractEmail(jwt);

        // 이메일로 사용자 조회
        User senderUser = chatService.getUserByEmail(email);

        log.info("메시지 송신: roomId={}, sender={}, message={}",
                messageDto.roomId(), email, messageDto.message());

        // DB 저장 처리
        ChatMessage savedMessage = chatService.sendChatMessage(messageDto, senderUser.getId());

        SendMessageResponseDto.Data data = new SendMessageResponseDto.Data(
                savedMessage.getId(),
                savedMessage.getChatRoomId(),
                savedMessage.getSender().getId(),
                savedMessage.getContent(),
                savedMessage.getSentAt()
        );

        String destination = "/sub/chat/" + savedMessage.getChatRoomId();
        messagingTemplate.convertAndSend(destination, data);
    }

}
