package com.example.demo.domain.chat.service;

import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.exception.ChatAccessDeniedException;
import com.example.demo.domain.chat.exception.InvalidChatArgumentException;
import com.example.demo.domain.chat.repository.ChatMessageRepository;
import com.example.demo.domain.chat.service.ChatRoomService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessage sendChatMessage(SendMessageRequestDto dto) {
        ChatRoom chatRoom = chatRoomService.findByRoomId(dto.roomId());

        // 채팅방 참가자인지 검사
        if (!chatRoom.getSender().equals(dto.senderId()) && !chatRoom.getReceiver().equals(dto.senderId())) {
            throw new ChatAccessDeniedException();
        }

        if (dto.message() == null || dto.message().isBlank()) {
            throw new InvalidChatArgumentException();
        }

        User senderUser = userRepository.findById(dto.senderId())
                .orElseThrow(() -> new IllegalArgumentException("보낸 사용자 없음"));

        ChatMessage message = ChatMessage.builder()
                .chatRoomId(dto.roomId())
                .sender(senderUser)
                .content(dto.message())
                .isRead(false)
                .build();

        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getMessages(UUID roomId) {
        chatRoomService.findByRoomId(roomId);  // 방 존재 확인용
        return chatMessageRepository.findByChatRoomId(roomId);
    }

    // 다른 메서드들은 필요에 따라 추가 구현하세요 (예: 이미지 전송, 읽음 처리 등)
}
