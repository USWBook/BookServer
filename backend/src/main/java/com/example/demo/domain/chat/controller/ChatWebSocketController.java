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

import java.util.UUID;

@Controller
public class ChatWebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(SendMessageRequestDto messageDto, Authentication authentication) {
        String userEmail = authentication.getName();

        // 이메일 → User 조회 후 UUID 추출 (sendChatMessage 메서드가 UUID 필요)
        User senderUser = chatService.getUserByEmail(userEmail);
        UUID senderId = senderUser.getId();

        log.info("메시지 송신: roomId={}, sender={}, message={}",
                messageDto.roomId(), authentication.getName(), messageDto.message());

        // DB 저장 처리
        ChatMessage savedMessage = chatService.sendChatMessage(messageDto, senderId);

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
