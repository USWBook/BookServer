package com.example.demo.domain.chat.controller;

import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
import com.example.demo.domain.chat.dto.response.SendMessageResponseDto;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.service.ChatService;
import com.example.demo.global.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequestDto messageDto, Principal principal) {

        if (principal == null) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        // Principal 객체에서 CustomUserDetails 추출
        CustomUserDetails customUserDetails = (CustomUserDetails) ((org.springframework.security.authentication.
                UsernamePasswordAuthenticationToken) principal).getPrincipal();

        UUID senderId = customUserDetails.getId();
        String senderEmail = customUserDetails.getEmail();

        log.info("메시지 수신: roomId={}, senderId={}, senderEmail={}, message={}",
                messageDto.roomId(), senderId, senderEmail, messageDto.message());

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
