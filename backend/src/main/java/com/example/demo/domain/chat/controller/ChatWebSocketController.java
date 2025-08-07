package com.example.demo.domain.chat.controller;

import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // 메시지 수신 ("send"는 프런트 JS에서 /pub/chat.send로 publish)
    @MessageMapping("/chat.send")
    public void sendMessage(SendMessageRequestDto message) {
        String sub = "/sub/chat/" + message.roomId();
        messagingTemplate.convertAndSend(sub, message);
    }
}
