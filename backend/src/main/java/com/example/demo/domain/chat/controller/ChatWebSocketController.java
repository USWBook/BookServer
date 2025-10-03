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
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    @MessageMapping("/pub/chat.send")
    public void sendMessage(SendMessageRequestDto messageDto, Principal principal) {

        if (principal == null) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        // Principal에서 이메일 추출
        String email = principal.getName();
        User senderUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 사용자를 찾을 수 없습니다."));

        log.info("메시지 송신: roomId={}, sender={}, message={}",
                messageDto.roomId(), senderUser.getEmail(), messageDto.message());

        // 메시지 저장
        ChatMessage savedMessage = chatService.sendChatMessage(messageDto, senderUser.getId());

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
