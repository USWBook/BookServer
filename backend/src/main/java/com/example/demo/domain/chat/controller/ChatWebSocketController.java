package com.example.demo.domain.chat.controller;

import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
import com.example.demo.domain.chat.dto.response.SendMessageResponseDto;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.service.ChatService;
import com.example.demo.domain.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

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
    public void sendMessage(SendMessageRequestDto messageDto, Principal principal) {

        if (principal == null) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        User senderUser = chatService.getUserByEmail(principal.getName());

        log.info("메시지 송신: roomId={}, sender={}, message={}",
                messageDto.roomId(), senderUser.getEmail(), messageDto.message());

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
