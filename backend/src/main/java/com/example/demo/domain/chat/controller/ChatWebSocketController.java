package com.example.demo.domain.chat.controller;

import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
import com.example.demo.domain.chat.dto.response.SendMessageResponseDto;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.service.ChatService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserRepository userRepository;  // user 조회용
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    @MessageMapping("/chat.send")
    public void sendMessage(SendMessageRequestDto messageDto, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        String email = authentication.getName();
        User senderUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 사용자를 찾을 수 없습니다."));

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
