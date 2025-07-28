package com.example.demo.global.config.chat;

import com.example.demo.domain.chat.entity.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * Redis에서 메시지가 publish되면 호출되어, JSON -> ChatMessage 역직렬화 후 WebSocket으로 발송
     */
    public void sendMessage(String publishMessage) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(publishMessage, ChatMessage.class);
            messagingTemplate.convertAndSend("/sub/chat/room/" + chatMessage.getChatRoomId(), chatMessage);
        } catch (Exception e) {
            log.error("RedisSubscriber 예외 발생: {}", e.getMessage(), e);
        }
    }
}
